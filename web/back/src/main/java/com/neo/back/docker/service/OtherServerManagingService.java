package com.neo.back.docker.service;

import com.neo.back.docker.dto.MyServerInfoDto;
import com.neo.back.docker.entity.DockerServer;
import com.neo.back.docker.exception.DoNotHaveServerException;
import com.neo.back.docker.repository.DockerServerRepository;
import com.neo.back.springjwt.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Transactional
@RequiredArgsConstructor
public class OtherServerManagingService {
    private final DockerServerRepository dockerServerRepo;
    public Mono<Object> getServerInfo (User user) {
        try {
            DockerServer dockerServer = dockerServerRepo.findByUser(user);
            if (dockerServer == null) throw new DoNotHaveServerException();

            MyServerInfoDto serverInfo = new MyServerInfoDto(
                    dockerServer.getServerName(),
                    dockerServer.getEdgeServer().getExternalIp(),
                    dockerServer.getPort(),
                    dockerServer.getGame().getGameName(),
                    dockerServer.getGame().getVersion(),
                    dockerServer.getRAMCapacity(),
                    dockerServer.isPublic(),
                    dockerServer.isFreeAccess(),
                    dockerServer.getServerComment()
            );

            return Mono.just(serverInfo);
        } catch (DoNotHaveServerException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("This user does not have an open server"));
        }
    }

    public  Mono<Object> setPublic (User user) {
        try {
            DockerServer dockerServer = dockerServerRepo.findByUser(user);
            if (dockerServer == null) throw new DoNotHaveServerException();

            dockerServer.setPublic(!dockerServer.isPublic());
            return  Mono.just(dockerServer.isPublic());
        } catch (DoNotHaveServerException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("This user does not have an open server"));
        }
    }

    public  Mono<Object> setFreeAccess (User user) {
        try {
            DockerServer dockerServer = dockerServerRepo.findByUser(user);
            if (dockerServer == null) throw new DoNotHaveServerException();

            dockerServer.setFreeAccess(!dockerServer.isFreeAccess());
            return  Mono.just(dockerServer.isFreeAccess());
        } catch (DoNotHaveServerException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("This user does not have an open server"));
        }
    }

    public  Mono<Object> setComment (User user, String comment) {
        try {
            DockerServer dockerServer = dockerServerRepo.findByUser(user);
            if (dockerServer == null) throw new DoNotHaveServerException();

            dockerServer.setServerComment(comment);
            return Mono.just("success set comment");
        } catch (DoNotHaveServerException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("This user does not have an open server"));
        }
    }

    public  Mono<Object> setTags (User user) {
        // try {
        //     DockerServer dockerServer = dockerServerRepo.findByUser(user);
        //     if (dockerServer == null) throw new DoNotHaveServerException();

        //     dockerServer.setServerComment(comment);
        //     return Mono.just("success set comment");
        // } catch (DoNotHaveServerException e) {
        //     return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("This user does not have an open server"));
        // }
        return null;
    }
}
