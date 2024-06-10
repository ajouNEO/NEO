package com.neo.back.mainService.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.neo.back.mainService.dto.ServerFilterDto;
import com.neo.back.mainService.dto.ServerListDto;
import com.neo.back.mainService.service.SearchServerService;
import com.neo.back.mainService.service.ServerJoinService;
import com.neo.back.mainService.utility.GetCurrentUser;
import com.neo.back.authorization.entity.User;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class CommunityController {
    private final SearchServerService searchServerService;
    private final GetCurrentUser getCurrentUser;
    private final ServerJoinService serverJoinService;

    @GetMapping("/api/server/list")
    public List<ServerListDto> getServerList() {

        return searchServerService.getServerList();
    }

    @GetMapping("/api/server/list_filter")
    public List<ServerListDto> getServerList_filter(@RequestBody ServerFilterDto filter) {

        return searchServerService.getServerList_filter(filter);
    }

    @GetMapping("/api/server/info/{dockerNum}")
    public Mono<Object> getServerList(@PathVariable Long dockerNum) {
        User user = getCurrentUser.getUser();
        return searchServerService.getServerInfo(dockerNum, user);
    }
    @GetMapping("/api/User/Participant/server/list")
    public List<ServerListDto> getParticipantServers() {
        User user = getCurrentUser.getUser();
        return searchServerService.getParticipantServers(user);
    }

    @GetMapping("/api/User/Applicant/server/list")
    public List<ServerListDto> getApplicantServers() {
        User user = getCurrentUser.getUser();
        return searchServerService.getApplicantServersInfo(user);
    }

    @PostMapping("/api/server/application/{dockerNum}")
    public Mono<Object> application(@PathVariable Long dockerNum) {
        User user = getCurrentUser.getUser();
        return serverJoinService.application(dockerNum, user);
    }

}
