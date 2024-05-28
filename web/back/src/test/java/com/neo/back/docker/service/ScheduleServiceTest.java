package com.neo.back.docker.service;

import com.neo.back.service.service.CloseDockerService;
import com.neo.back.service.service.ScheduleService;
import com.neo.back.authorization.entity.User;
import com.neo.back.authorization.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;


import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class ScheduleServiceTest {

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;
    @Autowired
    private ScheduleService scheduleService;
    @Mock
    private UserRepository userRepository;

    @Mock
    private CloseDockerService closeDockerService;

    @Mock
    private ScheduledFuture<?> scheduledFuture;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scheduleService = new ScheduleService(userRepository, taskScheduler, closeDockerService);
    }

    @Test
    void testScheduleServiceEndWithPoints() throws ExecutionException, InterruptedException {
        // Given
        User user = new User();
        user.setPoints(1000L);
        String dockerId = "docker123";
        Instant startTime = Instant.now();


        // When
        scheduleService.scheduleServiceEndWithPoints(user, dockerId, startTime, 200L);

        // ExecutorService를 사용하여 별도의 스레드에서 1분 1초 후까지 대기
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                Thread.sleep(131000); // 1분 11초 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).get();
        // Then
        verify(taskScheduler, times(1)).schedule(any(Runnable.class), any(Instant.class));
        System.out.println("스케줄링이 취소되었습니다. 사용자 포인트: " + user.getPoints());

    }
};