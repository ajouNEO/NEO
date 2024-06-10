package com.neo.back.infoManaging.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neo.back.authorization.entity.User;
import com.neo.back.infoManaging.dto.ServerManagingDockerImageDto;
import com.neo.back.infoManaging.dto.ServerManagingDockerServerDto;
import com.neo.back.infoManaging.dto.ServerManagingEdgeServerDto;
import com.neo.back.infoManaging.dto.ServerManagingGameDto;
import com.neo.back.infoManaging.service.ServerManagingService;
import com.neo.back.service.utility.GetCurrentUser;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequiredArgsConstructor
public class ServerManagingController {

    private final GetCurrentUser getCurrentUser;
    private final ServerManagingService serverManagingService;
    
    @GetMapping("/api/admin/game")
    public ResponseEntity<Object> getGame() {
        User user = getCurrentUser.getUser();
        return this.serverManagingService.getGame(user);
    }

    @PutMapping("/api/admin/game")
    public ResponseEntity<Object> setGame(@RequestBody ServerManagingGameDto inquiryData) {
        User user = getCurrentUser.getUser();
        return this.serverManagingService.setGame(user,inquiryData);
    }

    @GetMapping("/api/admin/dockerimage")
    public ResponseEntity<Object> getDockerImage() {
        User user = getCurrentUser.getUser();
        return this.serverManagingService.getDockerImage(user);
    }

    @PutMapping("/api/admin/dockerimage")
    public ResponseEntity<Object> setDockerImage(@RequestBody ServerManagingDockerImageDto DockerImageData) {
        User user = getCurrentUser.getUser();
        return this.serverManagingService.setDockerImage(user,DockerImageData);
    }

    @GetMapping("/api/admin/edgeserver")
    public ResponseEntity<Object> getEdgeServer() {
        User user = getCurrentUser.getUser();
        return this.serverManagingService.getEdgeServer(user);
    }

    @PutMapping("/api/admin/edgeserver")
    public ResponseEntity<Object> setEdgeServer(@RequestBody ServerManagingEdgeServerDto EdgeserverData) {
        User user = getCurrentUser.getUser();
        return this.serverManagingService.setEdgeServer(user,EdgeserverData);
    }

    @GetMapping("/api/admin/dockerserver")
    public ResponseEntity<Object> getDockerServer() {
        User user = getCurrentUser.getUser();
        return this.serverManagingService.getDockerServer(user);
    }

    @PutMapping("/api/admin/dockerserver")
    public ResponseEntity<Object> setDockerServer(@RequestBody ServerManagingDockerServerDto dockerServerData) {
        User user = getCurrentUser.getUser();
        return this.serverManagingService.setDockerServer(user,dockerServerData);
    }
}
