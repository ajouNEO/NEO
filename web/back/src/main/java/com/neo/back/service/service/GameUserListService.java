package com.neo.back.service.service;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.cj.x.protobuf.MysqlxDatatypes.Object;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.neo.back.authorization.entity.User;
import com.neo.back.service.dto.UserBanMineDto;
import com.neo.back.service.dto.UserBanServerListDto;
import com.neo.back.service.dto.UserListDto;
import com.neo.back.service.dto.UserSettingCMDDto;
import com.neo.back.service.entity.DockerServer;
import com.neo.back.service.middleware.DockerAPI;
import com.neo.back.service.repository.DockerServerRepository;
import com.neo.back.service.utility.MakeWebClient;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GameUserListService {
    private final DockerServerRepository dockerServerRepo;
    private final MakeWebClient makeWebClient;
    private final DockerAPI dockerAPI;
    private WebClient dockerWebClient;

    public Mono<Set<UserBanServerListDto>> getUser_banlist(User user) {
        UserSettingCMDDto UserSetting = dockerAPI.settingIDS_CMD(user);
        this.dockerWebClient = makeWebClient.makeDockerWebClient(UserSetting.getIp());
        ObjectMapper objectMapper = new ObjectMapper();
        String[] CMD_exec = new String[1];
        int CMD_exec_send_Ban = 0;

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
                List<UserBanMineDto> users = objectMapper.readValue(response, new TypeReference<List<UserBanMineDto>>() {});
                Set<UserBanServerListDto> UserList = new HashSet<>();

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
        setUserListCMD(user);
        String ack = AckUserList(user);
        String[] lines = ack.split("\n");

        for (String line : lines) {
            if (line.startsWith("Users:")) {
                data.setNumber(Integer.parseInt(line.substring(7).trim()));

            } else if (line.startsWith("name:")) {
                String str = line.substring(6).trim();
                data.getName().add(str);
            }
        }
        saveUserList(user);
        return Mono.just(data);
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

        dockerServer.getUserNameInGame()
        .forEach(name->{

        });

        for (String line : lines) {
            if (line.startsWith("Users:")) {
                data.setNumber(Integer.parseInt(line.substring(7).trim()));

            } else if (line.startsWith("name:")) {
                String str = line.substring(6).trim();
                data.getName().add(str);
            }
        }

        dockerServer.setUserNumber(data.getNumber());

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

        dockerServerRepo.save(dockerServer);

    }
}
