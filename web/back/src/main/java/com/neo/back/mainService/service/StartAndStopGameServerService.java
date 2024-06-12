package com.neo.back.mainService.service;

import com.neo.back.authorization.entity.User;
import com.neo.back.exception.DoNotHaveServerException;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.neo.back.mainService.dto.GameServerRunDto;
import com.neo.back.mainService.dto.UserSettingCMDDto;
import com.neo.back.mainService.middleware.DockerAPI;
import com.neo.back.mainService.utility.MakeWebClient;

import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class StartAndStopGameServerService {
    private WebClient dockerWebClient;
    private final MakeWebClient makeWebClient;
    private final DockerAPI dockerAPI;
    private final GameUserListService gameUserListService;

    public Mono<Object> getStartGameServer(User user) {
        try {
            UserSettingCMDDto UserSetting = dockerAPI.settingIDS_CMD(user);
            this.dockerWebClient = makeWebClient.makeDockerWebClient(UserSetting.getIp());
            String[] CMD_exec = new String[5];
            int CMD_exec_MEM = 0;
            int CMD_exec_send = 1;
            int CMD_exec_ACK = 2;
            int CMD_exec_path = 3;
            int CMD_exec2 = 4;
            String ack = null;
            CMD_exec[CMD_exec_send] = "CmdStartStr";
            GameServerRunDto startGameServerDto = new GameServerRunDto();

            UserSetting.getGameDockerAPICMDs_settings()
            .stream()
            .forEach(gameDockerAPICMD-> {
                if(gameDockerAPICMD.getCmdKind().equals("execCMD")){
                    CMD_exec[CMD_exec_MEM] = gameDockerAPICMD.getCmdId();
                }
                else if(gameDockerAPICMD.getCmdKind().equals("start_ack")){
                    CMD_exec[CMD_exec_ACK] = gameDockerAPICMD.getCmdId();
                }
                else if(gameDockerAPICMD.getCmdKind().equals("pathFolder")){
                    CMD_exec[CMD_exec_path] = gameDockerAPICMD.getCmdId();
                }
                else if(gameDockerAPICMD.getCmdKind().equals("execCMD2")){
                    CMD_exec[CMD_exec2] = gameDockerAPICMD.getCmdId();
                }
            });
            
            this.gameUserListService.setUserListCMD(user);

            if(CMD_exec[CMD_exec2] != null){
                // 차후에 맵과 플러그인 연동할 것
                String inputTEXT = "-world 1 -autocreate 1";
                this.dockerAPI.MAKEexec(CMD_exec[CMD_exec2], UserSetting.getDockerId(), this.dockerWebClient,"TEXT",inputTEXT)
                .block();
            }

            this.dockerAPI.MAKEexec(CMD_exec[CMD_exec_MEM], UserSetting.getDockerId(), this.dockerWebClient,"MEMORY",UserSetting.getMemory())
            .block();

            this.dockerAPI.MAKEexec(CMD_exec[CMD_exec_path], UserSetting.getDockerId(), this.dockerWebClient)
            .block();

            this.dockerAPI.MAKEexec(CMD_exec[CMD_exec_send], UserSetting.getDockerId(), this.dockerWebClient)
            .block();

            ack = this.dockerAPI.MAKEexec(CMD_exec[CMD_exec_ACK], UserSetting.getDockerId(), this.dockerWebClient)
            .block();
            startGameServerDto.setIsWorking(ack.equals("startAck\n"));
            return Mono.just(startGameServerDto);
        } catch (DoNotHaveServerException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("This user does not have an open server"));
        }
    }

    public Mono<Object> getStopGameServer(User user) {
        try {
            UserSettingCMDDto UserSetting = dockerAPI.settingIDS_CMD(user);
            this.dockerWebClient = makeWebClient.makeDockerWebClient(UserSetting.getIp());
            String[] CMD_exec = new String[2];
            int CMD_exec_send = 0;
            int CMD_exec_ACK = 1;
            String ack = null;
            GameServerRunDto startGameServerDto = new GameServerRunDto();

            UserSetting.getGameDockerAPICMDs_settings()
            .stream()
            .forEach(gameDockerAPICMD-> {
                if(gameDockerAPICMD.getCmdKind().equals("stop_ack")){
                    CMD_exec[CMD_exec_ACK] = gameDockerAPICMD.getCmdId();
                }
                else if(gameDockerAPICMD.getCmdKind().equals("CmdStopStr")){
                    CMD_exec[CMD_exec_send] = gameDockerAPICMD.getCmdId();
                }
            });

            this.dockerAPI.MAKEexec(CMD_exec[CMD_exec_send], UserSetting.getDockerId(), this.dockerWebClient)
            .block();

            ack = this.dockerAPI.MAKEexec(CMD_exec[CMD_exec_ACK], UserSetting.getDockerId(), this.dockerWebClient)
            .block();
            
            startGameServerDto.setIsWorking(ack.equals("stopAck\n"));
            return Mono.just(startGameServerDto);
        } catch (DoNotHaveServerException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("This user does not have an open server"));
        }
    }
}