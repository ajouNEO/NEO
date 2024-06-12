package com.neo.back.mainService.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.neo.back.mainService.dto.ServerFilterDto;
import com.neo.back.mainService.dto.ServerInfoDto;
import com.neo.back.mainService.dto.ServerListDto;
import com.neo.back.mainService.entity.DockerServer;
import com.neo.back.mainService.entity.GameTag;
import com.neo.back.mainService.repository.DockerServerRepository;
import com.neo.back.utility.RedisUtil;
import com.neo.back.authorization.entity.User;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SearchServerService {
    private final DockerServerRepository dockerServerRepo;
    private final RedisUtil redisUtil;

    public List<ServerListDto> getServerList () {
        List<DockerServer> dockerServers = dockerServerRepo.findByIsPublic(true);

        return dockerServers.stream()
            .map(server -> new ServerListDto(server.getId(), 
            server.getServerName(), 
            server.getUser().getName(), 
            server.getGame().getGameName(), 
            server.getGame().getVersion(), 
            server.isFreeAccess(),
            server.getGameTagNames(),
            redisUtil.getServerStatusInRedis(server.getId()),
            redisUtil.getUserNumberFromRedis(server.getId()),
            server.getMaxPlayer()))
            .collect(Collectors.toList());
    }

    public List<ServerListDto> getServerList_filter(ServerFilterDto filter) {
        List<DockerServer> dockerServers = dockerServerRepo.findByIsPublic(true);
        Iterator<DockerServer> iterator = dockerServers.iterator();
        while (iterator.hasNext()) {
            DockerServer dockerServer = iterator.next();
            if (checkGameName(dockerServer,filter)) {
                iterator.remove(); 
                continue;
            }

            if (checkVersion(dockerServer,filter)) {
                iterator.remove(); 
                continue;
            }

            if (checkIs_free_access(dockerServer,filter)) {
                iterator.remove(); 
                continue;
            }

            if (checkTags(dockerServer,filter)) {
                iterator.remove(); 
                continue;
            }

        }

        return dockerServers.stream()
            .map(server -> new ServerListDto(server.getId(), 
            server.getServerName(), 
            server.getUser().getName(), 
            server.getGame().getGameName(), 
            server.getGame().getVersion(), 
            server.isFreeAccess(),
            server.getGameTagNames(),
            redisUtil.getServerStatusInRedis(server.getId()),
            redisUtil.getUserNumberFromRedis(server.getId()),
            server.getMaxPlayer()))
            .collect(Collectors.toList());
    }

    private Boolean checkTags(DockerServer dockerServer,ServerFilterDto filter){
        Boolean flagPass = false;
        Boolean flagIS = false;
        if(filter.getTags() == null){
            return false;
        }
        for(String tag_filter : filter.getTags()){
            flagIS = false;
            for(GameTag tag_server : dockerServer.getTags()){
                if(tag_filter.equals(tag_server.getTag())){
                    flagIS = true;
                    break;
                }
            }
            if(!flagIS){
                flagPass = true;
                break;
            }
        }
        return flagPass;
    }

    private Boolean checkGameName(DockerServer dockerServer,ServerFilterDto filter){
        if(filter.getGame_name() == null){
            return false;
        }
        return !filter.getGame_name().equals(dockerServer.getGame().getGameName());
    }
    private Boolean checkVersion(DockerServer dockerServer,ServerFilterDto filter){
        if(filter.getVersion() == null){
            return false;
        }
        return !filter.getVersion().equals(dockerServer.getGame().getVersion());
    }
    private Boolean checkIs_free_access(DockerServer dockerServer,ServerFilterDto filter){
        if(filter.getIs_free_access() == null){
            return false;
        }
        return !filter.getIs_free_access().equals(dockerServer.isFreeAccess());
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
                dockerServer.getUser().getName(),
                dockerServer.getGame().getGameName(),
                dockerServer.getGame().getVersion(),
                dockerServer.isFreeAccess(),
                dockerServer.getServerComment(),
                dockerServer.getGameTagNames(),
                redisUtil.getServerStatusInRedis(dockerServer.getId()),
                redisUtil.getUserNumberFromRedis(dockerServer.getId()),
                dockerServer.getMaxPlayer()
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
          server.getUser().getName(), 
          server.getGame().getGameName(), 
          server.getGame().getVersion(), 
          server.isFreeAccess(),
          server.getGameTagNames(),
          redisUtil.getServerStatusInRedis(server.getId()),
          redisUtil.getUserNumberFromRedis(server.getId()),
          server.getMaxPlayer()))
        .collect(Collectors.toList());
    }

    public  List<ServerListDto> getApplicantServersInfo(User user) {
        List<DockerServer> dockerServers = dockerServerRepo.findAllByApplicantUserId(user);
        
        return dockerServers.stream()
        .map(server -> new ServerListDto(server.getId(),
         server.getServerName(),
          server.getUser().getName(), 
          server.getGame().getGameName(), 
          server.getGame().getVersion(), 
          server.isFreeAccess(),
          server.getGameTagNames(),
          redisUtil.getServerStatusInRedis(server.getId()),
          redisUtil.getUserNumberFromRedis(server.getId()),
          server.getMaxPlayer()))
        .collect(Collectors.toList());
    }

}
