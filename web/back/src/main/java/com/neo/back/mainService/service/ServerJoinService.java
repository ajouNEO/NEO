package com.neo.back.mainService.service;

import com.neo.back.mainService.entity.DockerServer;
import com.neo.back.mainService.repository.DockerServerRepository;
import com.neo.back.authorization.entity.User;
import com.neo.back.authorization.repository.UserRepository;
import com.neo.back.exception.DoNotHaveServerException;
import com.neo.back.exception.DoesNotPublicException;
import com.neo.back.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ServerJoinService {
    private final DockerServerRepository dockerServerRepo;
    private final UserRepository userRepo;
    private final Map<User, SseEmitter> getApplicantsEmitters = new ConcurrentHashMap<>();
    private final Map<User, SseEmitter> getParticipantsEmitters = new ConcurrentHashMap<>();

    public SseEmitter getApplicants(User user) {
        SseEmitter existingEmitter = getApplicantsEmitters.get(user);
        if (existingEmitter != null) {
            existingEmitter.complete();
        } 

        SseEmitter emitter = new SseEmitter();
        getApplicantsEmitters.put(user, emitter);

        sendApplicantsInitial(user, emitter);

        return emitter;
        
    }

    private void sendApplicantsInitial(User user, SseEmitter emitter) {
            try {
                DockerServer dockerServer = dockerServerRepo.findByUser(user);
    
                emitter.send(SseEmitter.event().data(dockerServer.getApplicantNames()));
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
    }

    public void sendApplicantsUpdate(DockerServer dockerServer) {
        SseEmitter emitter = getApplicantsEmitters.get(dockerServer.getUser());
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().data(dockerServer.getApplicantNames()));
            } catch (Exception e) {
                getApplicantsEmitters.remove(dockerServer.getUser());
            }
        }
    }

    public SseEmitter getParticipants(User user) {
        SseEmitter existingEmitter = getParticipantsEmitters.get(user);
        if (existingEmitter != null) {
            existingEmitter.complete();
        } 

        SseEmitter emitter = new SseEmitter();
        getParticipantsEmitters.put(user, emitter);

        sendParticipantsInitial(user, emitter);

        return emitter;
    }

    private void sendParticipantsInitial(User user, SseEmitter emitter) {
        try {
            DockerServer dockerServer = dockerServerRepo.findByUser(user);

            emitter.send(SseEmitter.event().data(dockerServer.getParticipantNames()));
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    public void sendParticipantsUpdate(DockerServer dockerServer) {
        SseEmitter emitter = getParticipantsEmitters.get(dockerServer.getUser());
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().data(dockerServer.getParticipantNames()));
            } catch (Exception e) {
                getParticipantsEmitters.remove(dockerServer.getUser());
            }
        }
    }

    public Mono<Object> application(Long dockerNum, User user) {
        DockerServer dockerServer;

        Optional<DockerServer> optionalDockerServer = dockerServerRepo.findById(dockerNum);
        if (optionalDockerServer.isPresent()) dockerServer = optionalDockerServer.get();
        else return Mono.error(new DoesNotPublicException());

        if (!dockerServer.isPublic()) return Mono.error(new DoesNotPublicException());

        dockerServer.addApplicant(user);
        dockerServerRepo.save(dockerServer);
        this.sendApplicantsUpdate(dockerServer);

        return Mono.just("success application");
    }

    public  Mono<Object> allowParticipation(User host, String participantName) {
        DockerServer dockerServer = dockerServerRepo.findByUser(host);
        if (dockerServer == null) return Mono.error(new DoNotHaveServerException());

        User participant = userRepo.findByName(participantName);
        if (participant == null) return Mono.error(new UserNotFoundException());

        dockerServer.addParticipant(participant);
        dockerServer.removeApplicant(participant);
        dockerServerRepo.save(dockerServer);
        this.sendApplicantsUpdate(dockerServer);
        this.sendParticipantsUpdate(dockerServer);

        return Mono.just("success");
    }

    public Mono<Object> refuseParticipation(User host, String participantName) {
        DockerServer dockerServer = dockerServerRepo.findByUser(host);
        if (dockerServer == null) return Mono.error(new DoNotHaveServerException());

        User participant = userRepo.findByName(participantName);
        if (participant == null) return Mono.error(new UserNotFoundException());

        dockerServer.removeApplicant(participant);
        dockerServerRepo.save(dockerServer);
        this.sendApplicantsUpdate(dockerServer);

        return Mono.just("success");
    }

    public  Mono<Object> deleteParticipation(User host, String participantName) {
        DockerServer dockerServer = dockerServerRepo.findByUser(host);
        if (dockerServer == null) return Mono.error(new DoNotHaveServerException());

        User participant = userRepo.findByName(participantName);
        if (participant == null) return Mono.error(new UserNotFoundException());

        dockerServer.removeParticipant(participant);
        dockerServerRepo.save(dockerServer);
        this.sendParticipantsUpdate(dockerServer);

        return Mono.just("success");
    }
}