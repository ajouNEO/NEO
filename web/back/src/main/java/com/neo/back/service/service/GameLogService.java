package com.neo.back.service.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.neo.back.service.dto.UserSettingDto;
import com.neo.back.service.middleware.DockerAPI;
import com.neo.back.service.utility.MakeWebClient;
import com.neo.back.authorization.entity.User;
import java.util.concurrent.*;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class GameLogService {
    private final Map<User, SseEmitter> getUserAndSseEmitter = new ConcurrentHashMap<>();
    private final Map<User, ScheduledExecutorService> getuserAndSche = new ConcurrentHashMap<>();
    private Map<User, String> previousLogs = new ConcurrentHashMap<>();
    private final DockerAPI dockerAPI;
    private final MakeWebClient makeWebClient;
    private WebClient dockerWebClient;
    
    public SseEmitter sendLogContinue(User user){
            UserSettingDto UserSetting = dockerAPI.settingIDS(user);
            this.dockerWebClient = makeWebClient.makeDockerWebClient(UserSetting.getIp());

            SseEmitter existingEmitter = getUserAndSseEmitter.get(user);
            ScheduledExecutorService existingExecutor = getuserAndSche.get(user);
            previousLogs.put(user, "");

            if (existingEmitter != null || existingEmitter != null) {
                existingEmitter.complete();
                existingExecutor.shutdown();
            }
            
            SseEmitter emitter = new SseEmitter();
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

            getUserAndSseEmitter.put(user, emitter);
            getuserAndSche.put(user, executor);
            executor.scheduleAtFixedRate(sendLogSche(user,UserSetting), 0, 1, TimeUnit.SECONDS);
            

        return emitter;
    }

    private Runnable sendLogSche(User user, UserSettingDto UserSetting) {
        return () -> {

            this.dockerAPI.MAKEexec("gameLog", UserSetting.getDockerId(), this.dockerWebClient)
            .subscribe(Log -> {
                try {
                    String previousLog = previousLogs.get(user);
                    if (!Log.equals(previousLog)) {
                        String newLog = Log.replace(previousLog, "");
                        getUserAndSseEmitter.get(user).send(SseEmitter.event().name("gameLogs").data(newLog));
                        previousLogs.put(user, Log);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        };
    }


}
