package com.neo.back.mainService.service;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.concurrent.*;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.neo.back.authorization.entity.User;
import com.neo.back.mainService.dto.UserBanMineDto;
import com.neo.back.mainService.dto.UserBanServerListDto;
import com.neo.back.mainService.dto.UserListDto;
import com.neo.back.mainService.dto.UserSettingCMDDto;
import com.neo.back.mainService.dto.UserSettingDto;
import com.neo.back.mainService.entity.DockerServer;
import com.neo.back.mainService.middleware.DockerAPI;
import com.neo.back.mainService.repository.DockerServerRepository;
import com.neo.back.utility.MakeWebClient;
import com.neo.back.utility.RedisUtil;

import io.jsonwebtoken.io.IOException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GameUserListService {
    private final DockerServerRepository dockerServerRepo;
    private final RedisUtil redisUtil;
    private final MakeWebClient makeWebClient;
    private final DockerAPI dockerAPI;
    private WebClient dockerWebClient;
    private final Map<User, SseEmitter> UserListSSE = new ConcurrentHashMap<>();
    private final Map<User, SseEmitter> UserBanListSSE = new ConcurrentHashMap<>();
    private final Map<User, ScheduledExecutorService> UserListSCH = new ConcurrentHashMap<>();
    private final Map<User, ScheduledExecutorService> UserBanListSCH = new ConcurrentHashMap<>();
    private Map<User, Set<UserBanServerListDto>> previousBanLists = new ConcurrentHashMap<>();

    public Mono<Set<UserBanServerListDto>> getUser_banlist(User user) {
        UserSettingCMDDto UserSetting = dockerAPI.settingIDS_CMD(user);
        this.dockerWebClient = makeWebClient.makeDockerWebClient(UserSetting.getIp());
        ObjectMapper objectMapper = new ObjectMapper();
        String[] CMD_exec = new String[1];
        int CMD_exec_send_Ban = 0;
        DockerServer dockerServer = dockerServerRepo.findByUser(user);
        String gameKind = dockerServer.getGame().getGameName();

        UserSetting.getGameDockerAPICMDs_settings()
        .stream()
        .forEach(gameDockerAPICMD-> {
            if(gameDockerAPICMD.getCmdKind().equals("userBan")){
                CMD_exec[CMD_exec_send_Ban] = gameDockerAPICMD.getCmdId();
            }
        });

        return this.dockerAPI.MAKEexec(CMD_exec[CMD_exec_send_Ban], UserSetting.getDockerId(), this.dockerWebClient)
        .flatMap(response -> {
            try {
                Set<UserBanServerListDto> UserList = new HashSet<>();
                if(response.equals("null\n")){
                    return Mono.just(UserList);
                }
                else if(gameKind.equals("Minecraft")){
                    List<UserBanMineDto> users = objectMapper.readValue(response, new TypeReference<List<UserBanMineDto>>() {});
                    for (UserBanMineDto banUser : users) {
                        UserBanServerListDto banItem = new UserBanServerListDto();
                        banItem.setName(banUser.getName());
                        banItem.setTime(banUser.getCreated());
                        banItem.setSource(banUser.getSource());
                        banItem.setReason(banUser.getReason());
                        banItem.setExpires(banUser.getExpires());
                        UserList.add(banItem);
                    }
                    return Mono.just(UserList);
                }
                else if(gameKind.equals("Terraria")){
                    String[] lines = response.split("\n");
                    for (String line : lines) {
                        UserBanServerListDto banItem = new UserBanServerListDto();
                        if (line.startsWith("//")) {
                            String name = line.substring(2).trim();
                            banItem.setName(name);
                            UserList.add(banItem);
                        }
                    }
                }
                return Mono.just(UserList);

            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public void setUserListCMD(User user){
        UserSettingCMDDto UserSetting = dockerAPI.settingIDS_CMD(user);
        this.dockerWebClient = makeWebClient.makeDockerWebClient(UserSetting.getIp());
        String[] CMD_exec = new String[1];
        int CMD_exec_send_cmd = 0;

        UserSetting.getGameDockerAPICMDs_settings()
        .stream()
        .forEach(gameDockerAPICMD-> {
            if(gameDockerAPICMD.getCmdKind().equals("userListCMD")){
                CMD_exec[CMD_exec_send_cmd] = gameDockerAPICMD.getCmdId();
            }
        });
        this.dockerAPI.MAKEexec(CMD_exec[CMD_exec_send_cmd], UserSetting.getDockerId(), this.dockerWebClient)
        .block();
    }

    
    public Mono<UserListDto> getUserlist(User user) {
        UserListDto data = new UserListDto();
        String ack = AckUserList(user);
        String[] lines = ack.split("\n");

        UserListSetting(data, lines);
        return Mono.just(data);
    }

    private void UserListSetting(UserListDto data, String[] lines) {
        for (String line : lines) {
            if (line.startsWith("Users:")) {
                data.setNumber(Integer.parseInt(line.substring(7).trim()));

            } else if (line.startsWith("name:")) {
                String str = line.substring(6).trim();
                data.getName().add(str);
            }
        }
    }

    public String AckUserList(User user){
        UserSettingCMDDto UserSetting = dockerAPI.settingIDS_CMD(user);
        this.dockerWebClient = makeWebClient.makeDockerWebClient(UserSetting.getIp());
        String[] CMD_exec = new String[1];
        int CMD_exec_send = 0;

        UserSetting.getGameDockerAPICMDs_settings()
        .stream()
        .forEach(gameDockerAPICMD-> {
            if(gameDockerAPICMD.getCmdKind().equals("userList")){
                CMD_exec[CMD_exec_send] = gameDockerAPICMD.getCmdId();
            }
        });

        return this.dockerAPI.MAKEexec(CMD_exec[CMD_exec_send], UserSetting.getDockerId(), this.dockerWebClient)
        .block();
    }

    @Transactional
    public void saveUserList(User user){
        UserListDto data = new UserListDto();
        String ack = AckUserList(user);
        String[] lines = ack.split("\n");
        DockerServer dockerServer = this.dockerServerRepo.findByUser(user);
        List<String> namesToRemove = null;
        List<String> namesToAdd = null;

        UserListSetting(data, lines);

        namesToRemove = dockerServer.getUserNameInGame()
        .stream()
        .filter(name -> !data.getName().contains(name))
        .collect(Collectors.toList());
        namesToRemove.forEach(dockerServer::removeUserName);
        
        namesToAdd = data.getName()
        .stream()
        .filter(name -> !dockerServer.getUserNameInGame().contains(name))
        .collect(Collectors.toList());
        namesToAdd.forEach(dockerServer::addUserName);


        redisUtil.updateUserNumberInRedis(dockerServer.getId(), data.getNumber());
        redisUtil.setUsernames(dockerServer.getDockerId() , data.getName());
        //dockerServerRepo.save(dockerServer);

    }

    public SseEmitter senduser_banlist(User user){
        UserSettingDto UserSetting = dockerAPI.settingIDS(user);
        this.dockerWebClient = makeWebClient.makeDockerWebClient(UserSetting.getIp());

        SseEmitter existingEmitter = UserBanListSSE.get(user);
        ScheduledExecutorService existingExecutor = UserBanListSCH.get(user);
        Set<UserBanServerListDto> UserBanServer = new HashSet<UserBanServerListDto>();
        UserBanServerListDto firstData= new UserBanServerListDto();
        firstData.setName("null");
        UserBanServer.add(firstData);
        previousBanLists.put(user,UserBanServer);

        if (existingEmitter != null || existingEmitter != null) {
            existingEmitter.complete();
            existingExecutor.shutdown();
        }
        
        SseEmitter emitter = new SseEmitter();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        UserBanListSSE.put(user, emitter);
        UserBanListSCH.put(user, executor);
        executor.scheduleAtFixedRate(sendBanListSCH(user,UserSetting), 0, 1, TimeUnit.SECONDS);
            

        return emitter;
    }
    private Runnable sendBanListSCH(User user, UserSettingDto UserSetting) {
        return () -> {
            this.getUser_banlist(user)
            .subscribe(BanList ->{
                if(!previousBanLists.get(user).equals(BanList)){
                    previousBanLists.get(user).retainAll(BanList);
                    previousBanLists.get(user).addAll(BanList);
                    try {
                        UserBanListSSE.get(user).send(SseEmitter.event().name("banList").data(previousBanLists.get(user)));
                        // UserBanListSSE.get(user).send(SseEmitter.event().name("banList").data(previousBanLists.get(user)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        };
    }

    public SseEmitter sendUserList(User user){
        UserSettingDto UserSetting = dockerAPI.settingIDS(user);
        this.dockerWebClient = makeWebClient.makeDockerWebClient(UserSetting.getIp());

        SseEmitter existingEmitter = UserListSSE.get(user);
        ScheduledExecutorService existingExecutor = UserListSCH.get(user);

        if (existingEmitter != null || existingEmitter != null) {
            existingEmitter.complete();
            existingExecutor.shutdown();
        }
        
        SseEmitter emitter = new SseEmitter();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        UserListSSE.put(user, emitter);
        UserListSCH.put(user, executor);
        executor.scheduleAtFixedRate(sendUserListSCH(user,UserSetting), 0, 1, TimeUnit.SECONDS);

        return emitter;
    }
    private Runnable sendUserListSCH(User user, UserSettingDto UserSetting) {
        return () -> {
            this.getUserlist(user)
            .subscribe(List ->{
                try {
                    UserListSSE.get(user).send(SseEmitter.event().name("userList").data(List));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            });

        };
    }
}
