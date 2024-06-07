package com.neo.back.service.service;

import com.neo.back.authorization.entity.User;
import com.neo.back.authorization.repository.UserRepository;
import com.neo.back.authorization.util.RedisUtil;
import com.neo.back.service.dto.ScheduledTaskDto;
import com.neo.back.service.entity.DockerServer;
import com.neo.back.service.repository.DockerServerRepository;
import com.neo.back.service.utility.TrackableScheduledFuture;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final RedisUtil redisUtil;
    private final UserRepository userRepository;
    private final DockerServerRepository dockerServerRepo;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final Map<String, TrackableScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final Map<String, TrackableScheduledFuture<?>> UserscheduledTasks = new ConcurrentHashMap<>();
    private final CloseDockerService closeDockerService;
    private final GameUserListService gameUserListService;

    public Mono<Object> startScheduling(User user) {
        Instant startTime = Instant.now();
        DockerServer dockerServer = dockerServerRepo.findByUser(user);
        String dockerId = dockerServer.getDockerId();
        Instant endTime = this.calculateEndTime(user.getPoints()/(dockerServer.getRAMCapacity()/2) - 1);

        redisUtil.setValue(user.getUsername(), String.valueOf(user.getPoints()));

        this.shutdownScheduling(user, dockerId, startTime, endTime);
        this.reducedPointsScheduling(user,dockerId, startTime, dockerServer.getRAMCapacity());
        this.startTrackingUser(user,dockerId);
        return Mono.just(ResponseEntity.ok("Container created successfully"));
    }

    public void stopScheduling(User user) {
        DockerServer dockerServer = dockerServerRepo.findByUser(user);
        String userdockerId = dockerServer.getDockerId();

        this.stopTrackingUser(userdockerId);
        this.cancelShutdownScheduling(userdockerId);
        this.cancelReducedPointsScheduling(user, userdockerId);
    }



    

    private void shutdownScheduling(User user, String dockerId, Instant startTime, Instant endTime) {
        Runnable task = () -> {
            closeDockerService.closeDockerService(user).block();
            this.cancelReducedPointsScheduling(user, dockerId);
            this.stopTrackingUser(dockerId);
        };
        ScheduledFuture<?> future = taskScheduler.schedule(task, endTime);
        TrackableScheduledFuture<?> trackableFuture = new TrackableScheduledFuture<>(future, task, dockerId, startTime, endTime);
        synchronized (scheduledTasks) {
            scheduledTasks.put(dockerId, trackableFuture);
        }
    }

    public void cancelShutdownScheduling(String dockerId) {
        TrackableScheduledFuture<?> taskInfo = scheduledTasks.get(dockerId);

        if (taskInfo != null) {
            taskInfo.cancel(true);
            synchronized (scheduledTasks) {
                scheduledTasks.remove(dockerId);
            }
        }
    }

    public void reducedPointsScheduling(User user,String dockerId, Instant startTime, int ramCapacity) {
        Runnable task = () -> tempUpdatePoints(user, ramCapacity / 2);
        ScheduledFuture<?> future = taskScheduler.scheduleWithFixedDelay(task, 60000);
        TrackableScheduledFuture<?> trackableFuture = new TrackableScheduledFuture<>(future, task, dockerId+"-point", startTime, null);
        scheduledTasks.put("point-" + dockerId , trackableFuture);
    }

    public void cancelReducedPointsScheduling(User user, String dockerId) {
        TrackableScheduledFuture<?> pointTask = scheduledTasks.get("point-"+dockerId);

        if (pointTask != null) {
            pointTask.cancel(true);
            synchronized (scheduledTasks) {
                scheduledTasks.remove("point-"+dockerId);
            }
        }
        updatePoints(user);
    }

    public void startTrackingUser(User user, String dockerId) {
        synchronized (UserscheduledTasks) {
            if (UserscheduledTasks.containsKey(dockerId)) {
                return;
            }
            Runnable task = () -> gameUserListService.saveUserList(user);
            ScheduledFuture<?> future = taskScheduler.scheduleWithFixedDelay(task, 10000);
            TrackableScheduledFuture<?> trackableFuture = new TrackableScheduledFuture<>(future, task, dockerId, Instant.now(), null);
            UserscheduledTasks.put(dockerId, trackableFuture);
        }
    }

    public void stopTrackingUser(String dockerId) {
        synchronized (UserscheduledTasks) {
            TrackableScheduledFuture<?> future = UserscheduledTasks.get(dockerId);
            if (future != null) {
                future.cancel(true);
                UserscheduledTasks.remove(dockerId);
            }
        }
    }



    



    private void tempUpdatePoints(User user, long usedPoints) {
        String userName = user.getUsername();

        // 캐시에서 현재 포인트를 가져오기
        String cachedPoints = redisUtil.getData(userName);
        if (cachedPoints != null) {
            long currentPoints = Long.parseLong(cachedPoints);
            long updatedPoints = currentPoints - usedPoints;
            redisUtil.setValue(userName, String.valueOf(updatedPoints));
        } else {
            // 캐시에 값이 없으면 데이터베이스에서 가져오기
            User dbUser = userRepository.findByUsername(userName);
            long currentPoints = dbUser.getPoints();
            long updatedPoints = currentPoints - usedPoints;
            redisUtil.setValue(userName, String.valueOf(updatedPoints));
        }
        System.out.println("Updated points in memory for user: " + user.getUsername() + " Points: " + redisUtil.getData(userName));

    }

    private void updatePoints(User user) {
        String email = user.getEmail();

        Long point = Long.valueOf(redisUtil.getData(email));
        user.setPoints(point);
        userRepository.save(user);
    }

    public Instant calculateEndTime(Long minutes) {
        return Instant.now().plus(Duration.ofMinutes(minutes));
    }

    //확인용
    public List<ScheduledTaskDto> getScheduledTasks() {
        // 불필요한 메모리 사용을 방지하기 위해 필터링을 추가합니다.
        return scheduledTasks.values().stream()
                .filter(future -> !future.isCancelled() && !future.isDone())
                .map(future -> new ScheduledTaskDto(future.getTaskId(), "scheduled", future.getStartTime(), future.getEndTime()))
                .collect(Collectors.toList());
    }

    public List<ScheduledTaskDto> getUserScheduledTasks() {
        // 불필요한 메모리 사용을 방지하기 위해 필터링을 추가합니다.
        return UserscheduledTasks.values().stream()
                .filter(future -> !future.isCancelled() && !future.isDone())
                .map(future -> new ScheduledTaskDto(future.getTaskId(), "scheduled", future.getStartTime(), future.getEndTime()))
                .collect(Collectors.toList());
    }
}
