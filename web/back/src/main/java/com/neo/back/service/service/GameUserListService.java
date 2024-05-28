package com.neo.back.service.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.neo.back.authorization.entity.User;
import com.neo.back.service.dto.UserSettingDto;
import com.neo.back.service.middleware.DockerAPI;
import com.neo.back.service.repository.DockerServerRepository;
import com.neo.back.service.utility.MakeWebClient;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GameUserListService {
    private final DockerServerRepository dockerServerRepo;
    private final MakeWebClient makeWebClient;
    private final DockerAPI dockerAPI;
    private WebClient dockerWebClient;

    public Mono<Object> getUser_banlist(User user) {
        UserSettingDto UserSetting = dockerAPI.settingIDS(user);
        this.dockerWebClient = makeWebClient.makeDockerWebClient(UserSetting.getIp());
        return this.dockerAPI.MAKEexec("banlist", UserSetting.getDockerId(), this.dockerWebClient)
        .flatMap(response -> {
            System.out.println(response);
            return null;
        })
        .onErrorResume(error -> { 
            return Mono.just("Error : " + error.getMessage());
        });
    }
}
