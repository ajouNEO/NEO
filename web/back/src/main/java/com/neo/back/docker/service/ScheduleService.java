// package com.neo.back.docker.service;


// import com.neo.back.springjwt.entity.User;
// import com.neo.back.springjwt.repository.UserRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
// import org.springframework.stereotype.Service;

// import java.time.Duration;
// import java.time.Instant;
// import java.util.Map;
// import java.util.concurrent.ConcurrentHashMap;
// import java.util.concurrent.ScheduledFuture;

// @Service
// @RequiredArgsConstructor
// public class ScheduleService {

//     private final UserRepository userRepository;

//     private final ThreadPoolTaskScheduler taskScheduler;

//     private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

//     private final CloseDockerService closeDockerService;

//     // 사용자 서비스 시작 및 종료 시간 스케줄링(포인트 기반)
//     public void scheduleServiceEndWithPoints(User user, String dockerId, Instant startTime,Long points){

//         Instant endTime = calculateEndTime(points);

//         scheduleTask(user,dockerId,startTime,endTime);
//     }

//     // 사용자 포인트 기반의 종료 시간 계산
//     public Instant calculateEndTime(Long points){
//         // 포인트 당 1분으로 계산. 100포인트는 1분
//         return Instant.now().plus(Duration.ofMinutes(points/100));

//     }


//     //사용자 서비스 조기 종료 및 스케줄 취소
//     public void cancelScheduledEnd(User user, String dockerId, Instant startTime,Instant endTime){
//         ScheduledFuture<?> future = scheduledTasks.get(dockerId);
//         if(future != null){
//             future.cancel(false);

//             updatePoints(user,startTime,endTime);

//             scheduledTasks.remove(dockerId);
//         }

//     }

//     // 실제 스케줄링 수행
//     private void scheduleTask(User user,String dockerId,Instant startTime,Instant endTime){
//         ScheduledFuture<?> future = taskScheduler.schedule(()->{
//             closeDockerService.closeDockerService(user);
//             updatePoints(user,startTime,endTime);
//         },endTime);
//         scheduledTasks.put(dockerId, future);

//     }

//     //포인트 소모 반영
//     private void updatePoints(User user,Instant startTime, Instant endTime){
//         long pointsUsed = calculatePointsUsed(startTime,endTime);
//         user.setPoints(user.getPoints()-pointsUsed);
//         System.out.println(user.getPoints());
//         userRepository.save(user);

//     }

//     // 포인트 소모량
//     private long calculatePointsUsed(Instant startTime, Instant endTime) {
//         long minutesUsed = Duration.between(startTime, endTime).toMinutes();
//         return minutesUsed * 100;
//     }

//     // scheduledTasks 맵을 반환하는 메서드 추가
//     public Map<String, ScheduledFuture<?>> getScheduledTasks() {
//         return scheduledTasks;
//     }
// /*
//     // 사용자 서비스 시작 및 종료 시간 스케줄링(기간권)

//     public void schedulerServiceEndWithDuration(String dockerId ,String duration){
//         Instant endTime = calculateEndTimeFromDuration(duration);
//         scheduleTask(dockerId,endTime);
//     }


//     //기간권 기반 종료 시간 계산
//     private Instant calculateEndTimeFromDuration(String duration){
//         switch(duration){
//             case "1일" :
//                 return Instant.now().plus(Duration.ofDays(1));
//             case "7일" :
//                 return Instant.now().plus(Duration.ofDays(7));

//             default:
//                 throw new IllegalArgumentException("지원하지 않는 기간입니다.");
//         }
//     }

// */
// }
