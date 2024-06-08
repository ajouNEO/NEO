package com.neo.back.service.service;

import com.neo.back.authorization.util.RedisUtil;
import com.neo.back.exception.DoNotHaveServerException;
import com.neo.back.service.dto.GameServerRunDto;
import com.neo.back.service.dto.MyServerInfoDto;
import com.neo.back.service.dto.ServerInputDto;
import com.neo.back.service.dto.UserSettingCMDDto;
import com.neo.back.service.dto.UserSettingDto;
import com.neo.back.service.entity.DockerServer;
import com.neo.back.service.entity.GameTag;
import com.neo.back.service.middleware.DockerAPI;
import com.neo.back.service.repository.DockerServerRepository;
import com.neo.back.service.repository.GameTagRepository;
import com.neo.back.service.utility.MakeWebClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.neo.back.authorization.entity.User;

import jakarta.el.ELException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OtherServerManagingService {
    private final DockerServerRepository dockerServerRepo;
    private final RedisUtil redisUtil;
    private final GameTagRepository gameTagRepo;
    private final DockerAPI dockerAPI;
    private final MakeWebClient makeWebClient;
    private final GameServerSettingService gameServerSettingService;
    private WebClient dockerWebClient;
    
    public Mono<Object> getServerInfo (User user) {
        DockerServer dockerServer = dockerServerRepo.findByUser(user);
        if (dockerServer == null) return Mono.error(new DoNotHaveServerException());

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
    }

    public  Mono<Object> setPublic (User user) {
        DockerServer dockerServer = dockerServerRepo.findByUser(user);
        if (dockerServer == null) return Mono.error(new DoNotHaveServerException());

        dockerServer.setPublic(!dockerServer.isPublic());
        dockerServerRepo.save(dockerServer);
        return  Mono.just(dockerServer.isPublic());
    }

    public  Mono<Object> setFreeAccess (User user) {
        DockerServer dockerServer = dockerServerRepo.findByUser(user);
        if (dockerServer == null) return Mono.error(new DoNotHaveServerException());

        dockerServer.setFreeAccess(!dockerServer.isFreeAccess());
        dockerServerRepo.save(dockerServer);
        return  Mono.just(dockerServer.isFreeAccess());
    }

    public  Mono<Object> setComment (User user, String comment) {
        DockerServer dockerServer = dockerServerRepo.findByUser(user);
        if (dockerServer == null) return Mono.error(new DoNotHaveServerException());

        dockerServer.setServerComment(comment);
        dockerServerRepo.save(dockerServer);
        return Mono.just("success set comment");
    }

    public  Mono<Object> getTags (User user) {
        DockerServer dockerServer = dockerServerRepo.findByUser(user);
        if (dockerServer == null) return Mono.error(new DoNotHaveServerException());
        Set<GameTag> tags = dockerServer.getTags();
        List<String> tags_data = new ArrayList<>();
        tags
        .stream()
        .forEach(data->{
            tags_data.add(data.getTag());
            
        });
        return Mono.just(tags_data);
    }

    @Transactional
    public  Mono<Object> setTags (User user, List<String> tags) {
        DockerServer dockerServer = dockerServerRepo.findByUser(user);
        if (dockerServer == null) return Mono.error(new DoNotHaveServerException());
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

    public Mono<GameServerRunDto> getServerRunning(User user) {
        UserSettingDto UserSetting = this.dockerAPI.settingIDS(user);
        this.dockerWebClient = makeWebClient.makeDockerWebClient(UserSetting.getIp());
        DockerServer dockerServer = dockerServerRepo.findByUser(user);
        String gameKind = dockerServer.getGame().getGameName();
        String[] CMD_exec = new String[2];
        int CMD_exec_send = 0;
        int CMD_exec_search = 1;
        
        UserSettingCMDDto UserSettingTo = this.dockerAPI.settingIDS_CMD(user);
        
        UserSettingTo.getGameDockerAPICMDs_settings()
        .stream()
        .forEach(gameDockerAPICMD-> {
            if(gameDockerAPICMD.getCmdKind().equals("serverRun")){
                CMD_exec[CMD_exec_send] = gameDockerAPICMD.getCmdId();
            }
            else if(gameDockerAPICMD.getCmdKind().equals("SearchStr")){
                CMD_exec[CMD_exec_search] = gameDockerAPICMD.getCmd();
            }
        });

        
        return this.dockerAPI.MAKEexec(CMD_exec[CMD_exec_send], UserSetting.getDockerId(), this.dockerWebClient)
        .flatMap(response -> {
            int count = 0;
            int index = 0;
    
            // 반복문을 통해 검색 문자열의 위치를 찾음
            if(response != null){
                while ((index = response.indexOf(CMD_exec[CMD_exec_search], index)) != -1) {
                    count++;
                    index += CMD_exec[CMD_exec_search].length(); // 검색 문자열의 길이만큼 인덱스를 증가시켜서 중복 카운트를 방지
                }
            }

            GameServerRunDto run = new GameServerRunDto();
            if (count == 3 && gameKind.equals("Minecraft")){ // 3의 값은 현재 프로세스가 돌고 있다 간주, 아니면 안돌고 있음
                run.setIsWorking(true);
            }
            else if(count == 2 && gameKind.equals("Palworld")){
                run.setIsWorking(true);
            }
            else if(count == 4 && gameKind.equals("Terraria")){
                run.setIsWorking(true);
            }
            else{
                run.setIsWorking(false);
            }
            redisUtil.setServerStatusInRedis(dockerServer.getId(), run.getIsWorking());

            return Mono.just(run);
        })
        .onErrorResume(error -> { // 값이 없음
            return Mono.just(new GameServerRunDto(false));
        });
    }

    public Mono<Object> getMaxPlayer(User user) {
        DockerServer dockerServer = dockerServerRepo.findByUser(user);
        if (dockerServer == null) return Mono.error(new DoNotHaveServerException());

        return gameServerSettingService.getServerSetting(user)
        .flatMap(response -> {
            // String responseBody = response.getBody();
            String responseBody;
            try {
                    if (response instanceof String) {
                        responseBody = (String) response;
                    } else {
                        // JSON 문자열로 변환
                        responseBody = new ObjectMapper().writeValueAsString(response);
                    }
                System.out.println(responseBody);
                JSONObject jsonObject = new JSONObject(responseBody);
                int maxPlayer = jsonObject.getInt(dockerServer.getGame().getMaxPlayerKey());
                dockerServer.setMaxPlayer(maxPlayer);
                dockerServerRepo.save(dockerServer);
                return Mono.just(maxPlayer);
            } catch (JsonProcessingException e) {
                throw new ELException();
            }
            
        });
    }
}
