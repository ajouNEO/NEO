package com.neo.back.docker.service;

import com.neo.back.docker.entity.DockerServer;
import com.neo.back.docker.exception.DoNotHaveServerException;
import com.neo.back.docker.exception.UserNotFoundException;
import com.neo.back.docker.repository.DockerServerRepository;
import com.neo.back.springjwt.entity.User;
import com.neo.back.springjwt.repository.UserRepository;

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
    private final UserRepository userRepo;
    private final Map<User, SseEmitter> getApplicantsEmitters = new ConcurrentHashMap<>();
    private final Map<User, SseEmitter> getParticipantsEmitters = new ConcurrentHashMap<>();

    public SseEmitter getApplicants(User user) {
        SseEmitter emitter = new SseEmitter();
        getApplicantsEmitters.put(user, emitter);

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

    public SseEmitter getParticipants(User user) {
        SseEmitter emitter = new SseEmitter();
        getParticipantsEmitters.put(user, emitter);

        emitter.onCompletion(() -> getParticipantsEmitters.remove(user));
        emitter.onTimeout(() -> getParticipantsEmitters.remove(user));
        emitter.onError(e -> getParticipantsEmitters.remove(user));

        sendParticipantsInitial(user, emitter);

        return emitter;
    }

    private void sendParticipantsInitial(User user, SseEmitter emitter) {
        try {
            DockerServer dockerServer = dockerServerRepo.findByUser(user);

            emitter.send(SseEmitter.event().data(dockerServer.getParticipantNames()));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    public void sendParticipantsUpdate(DockerServer dockerServer) {
        SseEmitter emitter = getParticipantsEmitters.get(dockerServer.getUser());
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().data(dockerServer.getParticipantNames()));
            } catch (IOException e) {
                getParticipantsEmitters.remove(dockerServer.getUser());
            }
        }
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

    public  Mono<Object> allowParticipation(User host, String participantName) {
        try {
            DockerServer dockerServer = dockerServerRepo.findByUser(host);
            if (dockerServer == null) throw new DoNotHaveServerException();

            User participant = userRepo.findByUsername(participantName);
            if (participant == null) throw new UserNotFoundException();

            dockerServer.addParticipant(participant);
            dockerServer.removeApplicant(participant);
            this.sendApplicantsUpdate(dockerServer);
            this.sendParticipantsUpdate(dockerServer);

            return Mono.just("success");
        } catch (DoNotHaveServerException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("This user does not have an open server"));
        } catch (UserNotFoundException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist"));
        }
    }

    public  Mono<Object> refuseParticipation(User host, String participantName) {
        try {
            DockerServer dockerServer = dockerServerRepo.findByUser(host);
            if (dockerServer == null) throw new DoNotHaveServerException();

            User participant = userRepo.findByUsername(participantName);
            if (participant == null) throw new UserNotFoundException();

            dockerServer.removeApplicant(participant);
            this.sendApplicantsUpdate(dockerServer);

            return Mono.just("success");
        } catch (DoNotHaveServerException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("This user does not have an open server"));
        } catch (UserNotFoundException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist"));
        }
    }
}