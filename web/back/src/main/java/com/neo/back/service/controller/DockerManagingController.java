package com.neo.back.service.controller;

import com.neo.back.service.dto.ScheduledTaskDto;
import com.neo.back.service.dto.UserSettingDto;
import com.neo.back.service.entity.DockerServer;
import com.neo.back.service.repository.DockerServerRepository;
import com.neo.back.service.service.ScheduleService;
import com.neo.back.service.utility.GetCurrentUser;
import com.neo.back.authorization.entity.User;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.neo.back.service.dto.CreateDockerDto;
import com.neo.back.service.dto.MyServerListDto;
import com.neo.back.service.service.CloseDockerService;
import com.neo.back.service.service.CreateDockerService;
import com.neo.back.service.service.UserServerListService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequiredArgsConstructor
public class DockerManagingController {
    
    private final UserServerListService userServerService;
    private final CreateDockerService createDockerService;
    private final CloseDockerService closeDockerService;
    private final GetCurrentUser getCurrentUser;

    private final ScheduleService scheduleService;

    private final DockerServerRepository dockerServerRepository;


    @GetMapping("/api/container/list")
    public Mono<List<MyServerListDto>> getMyServerList() {
        User user = getCurrentUser.getUser();
        return Mono.just(userServerService.getServerList(user));
    }

    @GetMapping("/api/container/containerid")
    public Mono<List<UserSettingDto>> getMyContainerid(){
        User user = getCurrentUser.getUser();
        return Mono.just(userServerService.getUserContainerId(user));
    }

    @DeleteMapping("/api/container/{imageNum}")
    public Mono<Object> deleteContainer(@PathVariable Long imageNum) {
        User user = getCurrentUser.getUser();
        return userServerService.deleteServer(imageNum, user);
    }

    @PostMapping("/api/container/create")
    public Mono<String> createContainer(@RequestBody CreateDockerDto config) {
        User user = getCurrentUser.getUser();

        return createDockerService.createContainer(config, user)
                .flatMap(result -> Mono.fromCallable(() -> {

                    Instant startTime = Instant.now();
                    System.out.println(user);
                    DockerServer dockerServer = dockerServerRepository.findByUser(user);
                    String dockerId = dockerServer.getDockerId();
                    Long points = user.getPoints();
                    scheduleService.scheduleServiceEndWithPoints(user, dockerId, startTime, points);
                    return "Container created successfully"; // containerId 포함하여 반환
                }))
                .onErrorResume(e -> {
                    // 에러 처리 로직
                    return Mono.just("Error creating container: " + e.getMessage());
                });
    }



    @PostMapping("/api/container/recreate")
    public Mono<Object> recreateContainer(@RequestBody CreateDockerDto config) {
        User user = getCurrentUser.getUser();
        return createDockerService.recreateContainer(config, user);
    }

    @PutMapping("/api/container/close")
    public Mono<Object> closeContainer() {
        User user = getCurrentUser.getUser();
        DockerServer dockerServer = dockerServerRepository.findByUser(user);
        String userdockerId = dockerServer.getDockerId();

        Optional<ScheduledTaskDto> scheduledTaskDto = scheduleService.getScheduledTasks().stream().filter(task -> task.getDockerId().equals(userdockerId)).findFirst();
        System.out.println(scheduledTaskDto);
        Instant startTime = dockerServer.getCreatedDate();

        Instant endTime = Instant.now();    // Assuming we don't have the actual end time here
        scheduleService.cancelScheduledEnd(user, userdockerId, startTime, endTime);
        return closeDockerService.closeDockerService(user);
    }

}