 package com.neo.back.service.service;


 import com.neo.back.authorization.entity.User;
 import com.neo.back.authorization.repository.UserRepository;
 import com.neo.back.authorization.util.RedisUtil;
 import com.neo.back.service.dto.ScheduledTaskDto;
 import lombok.RequiredArgsConstructor;
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
     // 1분당 point 변수로 설정하는게 좋았음. 고치려하니까 3군데나 코드 건들임 뺴먹으면? 버그.
     private final RedisUtil redisUtil;
     private final UserRepository userRepository;

     private final ThreadPoolTaskScheduler taskScheduler;

     private final Map<String, ScheduledTaskInfo> scheduledTasks = new ConcurrentHashMap<>();

     private final Map<String, ScheduledFuture<?>> UserscheduledTasks = new ConcurrentHashMap<>();

     private final CloseDockerService closeDockerService;

     private final GameUserListService gameUserListService;
     
     // 사용자 서비스 시작 및 종료 시간 스케줄링(포인트 기반)
     public void scheduleServiceEndWithPoints(User user, String dockerId, Instant startTime,Long points){

         Instant endTime = calculateEndTime(points);
         redisUtil.setValue(user.getUsername(), String.valueOf(user.getPoints()));

         scheduleTask(user,dockerId,startTime,endTime);
         schedulePointUpdatePerMinute(user, startTime, endTime);

     }

     // 사용자 포인트 기반의 종료 시간 계산
     public Instant calculateEndTime(Long points){
         // 포인트 당 1분으로 계산. 100포인트는 1분
         return Instant.now().plus(Duration.ofMinutes(points/1));
     }

     // 포인트를 메모리에서 주기적으로 업데이트
     public void schedulePointUpdatePerMinute(User user, Instant startTime, Instant endTime) {
         long totalMinutes = Duration.between(startTime, endTime).toMinutes();
         for (long i = 1; i <= totalMinutes; i++) {
             Instant scheduledTime = startTime.plus(Duration.ofMinutes(i));
             taskScheduler.schedule(() -> {
                 updateUserPointsInMemory(user, 1); // 1분마다 메모리에 포인트 업데이트
             }, scheduledTime);
         }
     }
     //사용자 서비스 조기 종료 및 스케줄 취소
     public void cancelScheduledEnd(User user, String dockerId, Instant startTime,Instant endTime){
         ScheduledTaskInfo taskInfo = scheduledTasks.get(dockerId);

         if (taskInfo != null) {
             taskInfo.getFuture().cancel(false);
             updatePoints(user, startTime, endTime);
             scheduledTasks.remove(dockerId);
             redisUtil.deleteData(user.getUsername());

         }

     }

     // 실제 스케줄링 수행
     private void scheduleTask(User user,String dockerId,Instant startTime,Instant endTime){
         ScheduledFuture<?> future = taskScheduler.schedule(()->{
             closeDockerService.closeDockerService(user).block();
             System.out.println("i'm here");
             updatePoints(user,startTime,endTime);
            scheduledTasks.remove(dockerId);
            UserscheduledTasks.remove(dockerId);

         },endTime);
         scheduledTasks.put(dockerId, new ScheduledTaskInfo(future, startTime, endTime));
     }

     // 메모리에서 포인트 업데이트
     private void updateUserPointsInMemory(User user, long minutesUsed) {
         String userName = user.getUsername();
         long pointsToDeduct = minutesUsed * 1; // Assuming 100 points per minute
         redisUtil.incrementValueBy(userName, -pointsToDeduct);
         System.out.println("Updated points in memory for user: " + user.getUsername() + " Points: " + redisUtil.getData(userName));
     }

     //포인트 소모 반영
     private void updatePoints(User user,Instant startTime, Instant endTime){
         long pointsUsed = calculatePointsUsed(startTime,endTime);
         System.out.println(user.getPoints());
         user.setPoints(user.getPoints()-pointsUsed);
         System.out.println(user.getPoints());
         userRepository.save(user);

     }

     // 포인트 소모량
     private long calculatePointsUsed(Instant startTime, Instant endTime) {
         long minutesUsed = Duration.between(startTime, endTime).toMinutes();
         return minutesUsed * 1;
     }

     // scheduledTasks 맵을 반환하는 메서드 추가
     public List<ScheduledTaskDto> getScheduledTasks() {
         return scheduledTasks.entrySet().stream()
                 .map(entry -> new ScheduledTaskDto(entry.getKey(), "scheduled", entry.getValue().getStartTime(), entry.getValue().getEndTime()))
                 .collect(Collectors.toList());
     }

     public void startTrackingUser(User user,String dockerId) {
         if (UserscheduledTasks.containsKey(dockerId)) {
             return; // 이미 스케줄링된 작업이 있으면 추가하지 않음
         }
         Runnable task = () -> gameUserListService.saveUserList(user);
         ScheduledFuture<?> future = taskScheduler.scheduleWithFixedDelay(task, 10000); // 10초마다 실행되도록 예시로 설정
         UserscheduledTasks.put(dockerId, future);
     }

     public void stopTrackingUser(String dockerId){
         ScheduledFuture<?> future = UserscheduledTasks.get(dockerId);

         if (future != null) {
             future.cancel(false);
         }
     }

     private static class ScheduledTaskInfo{
         private final ScheduledFuture<?> future;
         private final Instant startTime;
         private final Instant endTime;

         public ScheduledTaskInfo(ScheduledFuture<?> future, Instant startTime, Instant endTime) {
             this.future = future;
             this.startTime = startTime;
             this.endTime = endTime;
         }

         public ScheduledFuture<?> getFuture() {
             return future;
         }

         public Instant getStartTime() {
             return startTime;
         }

         public Instant getEndTime() {
             return endTime;
         }
     }

 /*
     // 사용자 서비스 시작 및 종료 시간 스케줄링(기간권)

     public void schedulerServiceEndWithDuration(String dockerId ,String duration){
         Instant endTime = calculateEndTimeFromDuration(duration);
         scheduleTask(dockerId,endTime);
     }


     //기간권 기반 종료 시간 계산
     private Instant calculateEndTimeFromDuration(String duration){
         switch(duration){
             case "1일" :
                 return Instant.now().plus(Duration.ofDays(1));
             case "7일" :
                 return Instant.now().plus(Duration.ofDays(7));

             default:
                 throw new IllegalArgumentException("지원하지 않는 기간입니다.");
         }
     }

 */
 }
