package com.neo.back.service.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.neo.back.service.dto.ServerInfoDto;
import com.neo.back.service.dto.ServerListDto;
import com.neo.back.service.entity.DockerServer;
import com.neo.back.service.repository.DockerServerRepository;
import com.neo.back.authorization.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SearchServerService {
    private final DockerServerRepository dockerServerRepo;

    public List<ServerListDto> getServerList () {
        List<DockerServer> dockerServers = dockerServerRepo.findByIsPublic(true);

        return dockerServers.stream()
            .map(server -> new ServerListDto(server.getId(), 
            server.getServerName(), 
            server.getUser().getUsername(), 
            server.getGame().getGameName(), 
            server.getGame().getVersion(), 
            server.isFreeAccess(),
            server.getGameTagNames()))
            .collect(Collectors.toList());
    }

    public Mono<Object> getServerInfo (Long id, User user) {
        String ip = null;
        int port = -1;
        DockerServer dockerServer;
        try {
            Optional<DockerServer> optionalDockerServer = dockerServerRepo.findById(id);
            if (optionalDockerServer.isPresent()) dockerServer = optionalDockerServer.get();
            else throw new AccessDeniedException("");

            if (!dockerServer.isPublic()) throw new AccessDeniedException("");

            if (!dockerServer.isFreeAccess()) {
                if (isParticipant(user, dockerServer)) {
                    ip = dockerServer.getEdgeServer().getExternalIp();
                    port = dockerServer.getPort();
                }
            } else {
                ip = dockerServer.getEdgeServer().getExternalIp();
                port = dockerServer.getPort();
            }

            ServerInfoDto serverInfo = new ServerInfoDto(
                dockerServer.getServerName(),
                ip,
                port,
                dockerServer.getUser().getUsername(),
                dockerServer.getGame().getGameName(),
                dockerServer.getGame().getVersion(),
                dockerServer.isFreeAccess(),
                dockerServer.getServerComment(),
                dockerServer.getGameTagNames()
            );
            return Mono.just(serverInfo);
        } catch (AccessDeniedException e) {
             return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body("Server that has not been disclosed or does not exist"));
        }
    }

    private boolean isParticipant(User user, DockerServer dockerServer) {
        return dockerServer.getParticipants().stream()
                .anyMatch(participant -> participant.equals(user));
    }

    public  List<ServerListDto> getParticipantServers(User user) {
        List<DockerServer> dockerServers = dockerServerRepo.findAllByParticipantUserId(user);
        return dockerServers.stream()
        .map(server -> new ServerListDto(server.getId(),
         server.getServerName(),
          server.getUser().getUsername(), 
          server.getGame().getGameName(), 
          server.getGame().getVersion(), 
          server.isFreeAccess(),
          server.getGameTagNames()))
        .collect(Collectors.toList());
    }

    public  List<ServerListDto> getApplicantServersInfo(User user) {
        List<DockerServer> dockerServers = dockerServerRepo.findAllByApplicantUserId(user);
        
        return dockerServers.stream()
        .map(server -> new ServerListDto(server.getId(),
         server.getServerName(),
          server.getUser().getUsername(), 
          server.getGame().getGameName(), 
          server.getGame().getVersion(), 
          server.isFreeAccess(),
          server.getGameTagNames()))
        .collect(Collectors.toList());
    }

}
