package com.neo.back.docker.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.neo.back.docker.dto.ServerInfoDto;
import com.neo.back.docker.dto.ServerListDto;
import com.neo.back.docker.entity.DockerServer;
import com.neo.back.docker.repository.DockerServerRepository;
import com.neo.back.springjwt.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@Transactional
@RequiredArgsConstructor
public class SearchServerService {
    private final DockerServerRepository dockerServerRepo;

    public List<ServerListDto> getServerList () {
        List<DockerServer> dockerServers = dockerServerRepo.findByIsPublic(true);

        return dockerServers.stream()
            .map(server -> new ServerListDto(server.getId(), server.getServerName(), server.getUser().getUsername(), server.getGame().getGameName(), server.getGame().getVersion(), server.isFreeAccess()))
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
                dockerServer.getServerComment()
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


}
