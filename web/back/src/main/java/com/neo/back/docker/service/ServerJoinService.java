package com.neo.back.docker.service;

import com.neo.back.docker.repository.DockerServerRepository;
import com.neo.back.springjwt.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Transactional
@RequiredArgsConstructor
public class ServerJoinService {
    private DockerServerRepository dockerServerRepo;
    private final List<SseEmitter> getApplicantsEmitters = new CopyOnWriteArrayList<>();

    public SseEmitter getApplicants(User user) {
        SseEmitter emitter = new SseEmitter();
        getApplicantsEmitters.add(emitter);

        // 연결 종료 시 emitters 리스트에서 제거
        emitter.onCompletion(() -> getApplicantsEmitters.remove(emitter));
        emitter.onTimeout(() -> getApplicantsEmitters.remove(emitter));
        emitter.onError(e -> getApplicantsEmitters.remove(emitter));

        sendApplicantsInitial(emitter, user);

        return emitter;
    }

    private void sendApplicantsInitial(SseEmitter emitter, User user) {
            try {
                // 초기 데이터 전송 (예: 데이터베이스에서 가져온 데이터)
                String initialData = "초기 데이터"; // 여기서 실제 데이터를 가져옵니다.
                emitter.send(SseEmitter.event().data(initialData));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
    }

    public void sendApplicantsUpdate(String message) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : getApplicantsEmitters) {
            try {
                emitter.send(SseEmitter.event().data(message));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }
        getApplicantsEmitters.removeAll(deadEmitters);
    }

    public Mono<Object> getParticipants(User user) {

        return null;
    }

    public Mono<Object> application(Long dockerNum, User user) {

        return null;
    }

    public  Mono<Object> allowParticipation(User host, User participant) {

        return null;
    }

    public  Mono<Object> refuseParticipation(User host, User participant) {

        return null;
    }
}