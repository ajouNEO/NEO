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

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    public void stopScheduling(User user) {
        DockerServer dockerServer = dockerServerRepo.findByUser(user);
        String userdockerId = dockerServer.getDockerId();

        Optional<ScheduledTaskDto> scheduledTaskDto = this.getScheduledTasks().stream().filter(task -> task.getDockerId().equals(userdockerId)).findFirst();
        System.out.println(scheduledTaskDto);
        Instant startTime = dockerServer.getCreatedDate();

        Instant endTime = Instant.now();    // Assuming we don't have the actual end time here
        this.cancelScheduledEnd(user, userdockerId, startTime, endTime);
        this.stopTrackingUser(userdockerId);
    }

    public void scheduleServiceEndWithPoints(User user, String dockerId, Instant startTime, Long points, int ramCapacity) {
        Instant endTime = calculateEndTime(points);
        redisUtil.setValue(user.getUsername(), String.valueOf(user.getPoints()));
        scheduleTask(user, dockerId, startTime, endTime);
        schedulePointUpdatePerMinute(user,dockerId, startTime, endTime, ramCapacity);
    }

    public Instant calculateEndTime(Long points) {
        return Instant.now().plus(Duration.ofMinutes(points));
    }


    public void cancelScheduledEnd(User user, String dockerId, Instant startTime, Instant endTime) {
        TrackableScheduledFuture<?> taskInfo = scheduledTasks.get(dockerId);
        System.out.println(taskInfo);
        TrackableScheduledFuture<?> pointTask = scheduledTasks.get("point-"+dockerId);

        System.out.println(pointTask);
        if (taskInfo != null) {
            boolean cancelled = taskInfo.cancel(true);

          //       pointTask1.cancel(true);
            pointTask.cancel(true);

            if (cancelled) {
                updatePoints(user, startTime, endTime);
                synchronized (scheduledTasks) {
                    scheduledTasks.remove(dockerId);
                    scheduledTasks.remove("point-"+dockerId);


                }
                System.out.println("Task cancelled and removed from scheduledTasks map for dockerId: " + dockerId);
            } else {
                System.out.println("Task cancellation failed for dockerId: " + dockerId);
            }
        } else {
            System.out.println("No task found for dockerId: " + dockerId);
        }
    }

    private void scheduleTask(User user, String dockerId, Instant startTime, Instant endTime) {
        Runnable task = () -> {
            try {
                closeDockerService.closeDockerService(user).block();
                updatePoints(user, startTime, endTime);
                TrackableScheduledFuture<?> pointTask = scheduledTasks.get("point-"+dockerId);
                pointTask.cancel(true);

            } finally {
                // 작업이 완료되면 맵에서 제거합니다.
                synchronized (scheduledTasks) {
                    scheduledTasks.remove(dockerId);
                }
                synchronized (UserscheduledTasks) {
                    UserscheduledTasks.remove(dockerId);
                }
            }
        };
        ScheduledFuture<?> future = taskScheduler.schedule(task, endTime);
        TrackableScheduledFuture<?> trackableFuture = new TrackableScheduledFuture<>(future, task, dockerId, startTime, endTime);
        synchronized (scheduledTasks) {
            scheduledTasks.put(dockerId, trackableFuture);
        }
    }

    //스케줄로직 재귀식으로 짜기? enddate인지 체크하고 enddate면 제거하고 아니면 스케줄에서 +1분해서 넣고.


    public void schedulePointUpdatePerMinute(User user,String dockerId, Instant startTime, Instant endTime, int ramCapacity) {
        Runnable task = () -> updateUserPointsInMemory(user, ramCapacity / 2);
        ScheduledFuture<?> future = taskScheduler.scheduleWithFixedDelay(task, 60000);
        TrackableScheduledFuture<?> trackableFuture = new TrackableScheduledFuture<>(future, task, dockerId+"-point", startTime, endTime);
        scheduledTasks.put("point-" + dockerId , trackableFuture);
    }


    private void updateUserPointsInMemory(User user, long minutesUsed) {
        String userName = user.getUsername();
        long pointsToDeduct = minutesUsed;

        // 캐시에서 현재 포인트를 가져오기
        String cachedPoints = redisUtil.getData(userName);
        if (cachedPoints != null) {
            long currentPoints = Long.parseLong(cachedPoints);
            long updatedPoints = currentPoints - pointsToDeduct;
            redisUtil.setValue(userName, String.valueOf(updatedPoints));
        } else {
            // 캐시에 값이 없으면 데이터베이스에서 가져오기
            User dbUser = userRepository.findByUsername(userName);
            long currentPoints = dbUser.getPoints();
            long updatedPoints = currentPoints - pointsToDeduct;
            redisUtil.setValue(userName, String.valueOf(updatedPoints));
        }
        System.out.println("Updated points in memory for user: " + user.getUsername() + " Points: " + redisUtil.getData(userName));

    }

    private void updatePoints(User user, Instant startTime, Instant endTime) {
        long pointsUsed = calculatePointsUsed(startTime, endTime);
        user.setPoints(user.getPoints() - pointsUsed);
        userRepository.save(user);
    }

    private long calculatePointsUsed(Instant startTime, Instant endTime) {
        long minutesUsed = Duration.between(startTime, endTime).toMinutes();
        return minutesUsed;
    }

    public List<ScheduledTaskDto> getScheduledTasks() {
        // 불필요한 메모리 사용을 방지하기 위해 필터링을 추가합니다.
        return scheduledTasks.values().stream()
                .filter(future -> !future.isCancelled() && !future.isDone())
                .map(future -> new ScheduledTaskDto(future.getTaskId(), "scheduled", future.getStartTime(), future.getEndTime()))
                .collect(Collectors.toList());
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
                System.out.println("User tracking task cancelled and removed for dockerId: " + dockerId);
            } else {
                System.out.println("No user tracking task found for dockerId: " + dockerId);
            }
        }
    }
}
