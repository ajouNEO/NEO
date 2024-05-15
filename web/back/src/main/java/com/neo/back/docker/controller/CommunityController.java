package com.neo.back.docker.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.neo.back.docker.dto.ServerListDto;
import com.neo.back.docker.service.SearchServerService;
import com.neo.back.docker.utility.GetCurrentUser;
import com.neo.back.springjwt.entity.User;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class CommunityController {
    private final SearchServerService searchServerService;
    private final GetCurrentUser getCurrentUser;

    @GetMapping("/api/server/list")
    public List<ServerListDto> getServerList() {

        return searchServerService.getServerList();
    }

    @GetMapping("/api/server/info/{dockerNum}")
    public Mono<Object> getServerList(@PathVariable Long dockerNum) {
        User user = getCurrentUser.getUser();
        return searchServerService.getServerInfo(dockerNum, user);
    }
}