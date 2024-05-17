package com.neo.back.docker.service;

import com.neo.back.docker.entity.DockerServer;
import com.neo.back.docker.repository.DockerServerRepository;
import com.neo.back.springjwt.entity.User;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
@RequiredArgsConstructor
public class ServerJoinService {
    private final DockerServerRepository dockerServerRepo;
    private final Map<User, SseEmitter> getApplicantsEmitters = new ConcurrentHashMap<>();

    public SseEmitter getApplicants(User user) {
        SseEmitter emitter = new SseEmitter();
        getApplicantsEmitters.put(user, emitter);

        //연결 종료 시 emitters 리스트에서 제거
        emitter.onCompletion(() -> getApplicantsEmitters.remove(user));
        emitter.onTimeout(() -> getApplicantsEmitters.remove(user));
        emitter.onError(e -> getApplicantsEmitters.remove(user));

        sendApplicantsInitial(user, emitter);

        return emitter;
    }

    private void sendApplicantsInitial(User user, SseEmitter emitter) {
            try {
                DockerServer dockerServer = dockerServerRepo.findByUser(user);
    
                emitter.send(SseEmitter.event().data(dockerServer.getApplicantNames()));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
    }

    public void sendApplicantsUpdate(DockerServer dockerServer) {
        SseEmitter emitter = getApplicantsEmitters.get(dockerServer.getUser());
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().data(dockerServer.getApplicantNames()));
            } catch (IOException e) {
                getApplicantsEmitters.remove(dockerServer.getUser());
            }
        }
    }

    public Mono<Object> getParticipants(User user) {

        return null;
    }

    public Mono<Object> application(Long dockerNum, User user) {
        DockerServer dockerServer;
        try {
            Optional<DockerServer> optionalDockerServer = dockerServerRepo.findById(dockerNum);
            if (optionalDockerServer.isPresent()) dockerServer = optionalDockerServer.get();
            else throw new AccessDeniedException("");

            if (!dockerServer.isPublic()) throw new AccessDeniedException("");

            dockerServer.addApplicant(user);
            dockerServerRepo.save(dockerServer);
            this.sendApplicantsUpdate(dockerServer);

            return Mono.just("success application");
        } catch (AccessDeniedException e) {
             return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body("Server that has not been disclosed or does not exist"));
        }
    }

    public  Mono<Object> allowParticipation(User host, User participant) {

        return null;
    }

    public  Mono<Object> refuseParticipation(User host, User participant) {

        return null;
    }
}