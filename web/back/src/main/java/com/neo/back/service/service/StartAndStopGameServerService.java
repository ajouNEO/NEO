package com.neo.back.service.service;

import java.util.Map;

import com.neo.back.authorization.entity.User;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.neo.back.service.dto.GameServerRunDto;
import com.neo.back.service.dto.UserSettingCMDDto;
import com.neo.back.service.dto.UserSettingDto;
import com.neo.back.service.entity.DockerServer;
import com.neo.back.service.exception.DoNotHaveServerException;
import com.neo.back.service.middleware.DockerAPI;
import com.neo.back.service.repository.DockerServerRepository;
import com.neo.back.service.repository.GameDockerAPICMDRepository;
import com.neo.back.service.utility.MakeWebClient;

import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class StartAndStopGameServerService {
    private final DockerServerRepository dockerServerRepo;
    private WebClient dockerWebClient;
    private final MakeWebClient makeWebClient;
    private final DockerAPI dockerAPI;
    private final GameDockerAPICMDRepository gameDockerAPICMDRepo;

    public Mono<Object> getStartGameServer(User user) {
        try {
            UserSettingCMDDto UserSetting = dockerAPI.settingIDS_CMD(user);
            this.dockerWebClient = makeWebClient.makeDockerWebClient(UserSetting.getIp());
            String[] CMD_exec = new String[3];
            int CMD_exec_MEM = 0;
            int CMD_exec_send = 1;
            int CMD_exec_ACK = 2;
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
            });
            
            this.dockerAPI.MAKEexec(CMD_exec[CMD_exec_MEM], UserSetting.getDockerId(), this.dockerWebClient,"MEMORY",UserSetting.getMemory())
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
            CMD_exec[CMD_exec_send] = "CmdStopStr";
            GameServerRunDto startGameServerDto = new GameServerRunDto();

            UserSetting.getGameDockerAPICMDs_settings()
            .stream()
            .forEach(gameDockerAPICMD-> {
                if(gameDockerAPICMD.getCmdKind().equals("stop_ack")){
                    CMD_exec[CMD_exec_ACK] = gameDockerAPICMD.getCmdId();
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
