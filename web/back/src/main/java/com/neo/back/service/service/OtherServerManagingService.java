package com.neo.back.service.service;

import com.neo.back.service.dto.MyServerInfoDto;
import com.neo.back.service.dto.ServerInputDto;
import com.neo.back.service.dto.UserSettingDto;
import com.neo.back.service.entity.DockerServer;
import com.neo.back.service.entity.GameTag;
import com.neo.back.service.exception.DoNotHaveServerException;
import com.neo.back.service.middleware.DockerAPI;
import com.neo.back.service.repository.DockerServerRepository;
import com.neo.back.service.repository.GameTagRepository;
import com.neo.back.service.utility.MakeWebClient;
import com.neo.back.authorization.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OtherServerManagingService {
    private final DockerServerRepository dockerServerRepo;
    private final GameTagRepository gameTagRepo;
    private final DockerAPI dockerAPI;
    private final MakeWebClient makeWebClient;
    private WebClient dockerWebClient;
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

    @Transactional
    public  Mono<Object> setTags (User user, List<String> tags) {
        try {
            DockerServer dockerServer = dockerServerRepo.findByUser(user);
            if (dockerServer == null) throw new DoNotHaveServerException();
            dockerServer.getGameTagNames()
            .forEach(tag ->{
                dockerServer.removeGameTag(gameTagRepo.findByTag(tag));
            });
            tags.forEach(tag ->{
                if(gameTagRepo.findByTag(tag) == null){
                    GameTag game = new GameTag();
                    game.setTag(tag);
                    gameTagRepo.save(game);
                    dockerServer.addGameTag(game);
                }
                else{
                    dockerServer.addGameTag(gameTagRepo.findByTag(tag));
                }
            });
            dockerServerRepo.save(dockerServer);
            return Mono.just("success set setTags");
        } catch (DoNotHaveServerException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("This user does not have an open server"));
        }
    }

    public Mono<String> sendInputToServer(User user, ServerInputDto input) {
        UserSettingDto UserSetting = dockerAPI.settingIDS(user);
        this.dockerWebClient = makeWebClient.makeDockerWebClient(UserSetting.getIp());
        return this.dockerAPI.MAKEexec("input", UserSetting.getDockerId(), this.dockerWebClient,"INPUT",input.getInput())
        .then(Mono.defer(() -> {
            return Mono.just("Input Success");
        }))
        .onErrorResume(error -> { // 값이 없음
            return Mono.just("Error : " + error.getMessage());
        });
    }
}
