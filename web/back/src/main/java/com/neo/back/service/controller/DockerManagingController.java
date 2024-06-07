package com.neo.back.service.controller;

import com.neo.back.service.dto.UserSettingDto;
import com.neo.back.service.service.*;
import com.neo.back.service.utility.GetCurrentUser;
import com.neo.back.authorization.entity.User;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.neo.back.service.dto.CreateDockerDto;
import com.neo.back.service.dto.MyServerListDto;

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
    public Mono<Object> createContainer(@RequestBody CreateDockerDto config) {
        User user = getCurrentUser.getUser();
        return createDockerService.createContainer(config, user);
    }

    @PostMapping("/api/container/recreate")
    public Mono<Object> recreateContainer(@RequestBody CreateDockerDto config) {
        User user = getCurrentUser.getUser();
        return createDockerService.recreateContainer(config, user);
    }

    @PutMapping("/api/container/close")
    public Mono<Object> closeContainer() {
        User user = getCurrentUser.getUser();
        
        scheduleService.stopScheduling(user);

        return closeDockerService.closeDockerService(user);
    }

}