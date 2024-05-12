package com.neo.back.docker.service;

import java.util.Map;

import com.neo.back.springjwt.entity.User;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.neo.back.docker.dto.StartGameServerDto;
import com.neo.back.docker.entity.DockerServer;
import com.neo.back.docker.exception.DoNotHaveServerException;
import com.neo.back.docker.middleware.DockerAPI;
import com.neo.back.docker.repository.DockerServerRepository;
import com.neo.back.docker.repository.GameDockerAPICMDRepository;
import com.neo.back.docker.utility.MakeWebClient;

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
            DockerServer dockerServer = this.dockerServerRepo.findByUser(user);
            if (dockerServer == null) throw new DoNotHaveServerException();

            String ip = dockerServer.getEdgeServer().getIp();
            String dockerId = dockerServer.getDockerId();
            int memory = dockerServer.getRAMCapacity();
            String memoryToStr = Integer.toString(memory);
            StartGameServerDto startGameServerDto = new StartGameServerDto();
            this.dockerWebClient =  this.makeWebClient.makeDockerWebClient(ip);
            
            String gameVersion = dockerServer.getGame().getVersion() + "_START";
            String CmdMemory = this.gameDockerAPICMDRepo.findBycmdId(gameVersion).getCmd();
            CmdMemory = CmdMemory.replace("MEMORY",memoryToStr);
            String[] CmdMemoryStr = this.dockerAPI.split_tap(CmdMemory);
            Map<String,Boolean> startExecRequest = this.dockerAPI.makeExecStartInst();
            Map<String,Object> setMemoryInst = this.dockerAPI.makeExecInst(CmdMemoryStr);
            Mono<Map> AckMemoryStr = this.dockerAPI.makeExec(dockerId, setMemoryInst, this.dockerWebClient);
            String MemoryMeoStr = (String) AckMemoryStr.block().get("Id");
            Mono<String> AckMeomoryEND = this.dockerAPI.startExec(MemoryMeoStr,startExecRequest, this.dockerWebClient);
            AckMeomoryEND.block();

            String CmdStart = this.gameDockerAPICMDRepo.findBycmdId("CmdStartStr").getCmd();
            String[] CmdStartStr = this.dockerAPI.split_tap(CmdStart);
            Map<String,Object> setStartInst = this.dockerAPI.makeExecInst(CmdStartStr);
            Mono<Map> AckStartStr = this.dockerAPI.makeExec(dockerId, setStartInst, this.dockerWebClient);
            String startMeoStr = (String) AckStartStr.block().get("Id");
            Mono<String> AckStartEND = this.dockerAPI.startExec(startMeoStr,startExecRequest, this.dockerWebClient);
            AckStartEND.block();

            String CmdStartAck = this.gameDockerAPICMDRepo.findBycmdId("CmdStartAckStr").getCmd();
            String[] CmdStartAckStr = this.dockerAPI.split_tap(CmdStartAck);
            Map<String,Object> setAckInst = this.dockerAPI.makeExecInst(CmdStartAckStr);
            Mono<Map> AckAckInfoStr = this.dockerAPI.makeExec(dockerId, setAckInst, this.dockerWebClient);
            String AckMeoStr = (String) AckAckInfoStr.block().get("Id");
            Mono<String> AckAckInfoEND = this.dockerAPI.startExec(AckMeoStr,startExecRequest, this.dockerWebClient);
            String Ack = (String)AckAckInfoEND.block();
            startGameServerDto.setIsWorking(Ack.equals("startAck"));
            System.out.println(startGameServerDto.getIsWorking());
            return Mono.just(startGameServerDto);
        } catch (DoNotHaveServerException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("This user does not have an open server"));
        }
    }

    public Mono<Object> getStopGameServer(User user) {
        try {
            DockerServer dockerServer = dockerServerRepo.findByUser(user);
            if (dockerServer == null) throw new DoNotHaveServerException();

            String ip = dockerServer.getEdgeServer().getIp();
            String dockerId = dockerServer.getDockerId();
            StartGameServerDto startGameServerDto = new StartGameServerDto();
            this.dockerWebClient =  this.makeWebClient.makeDockerWebClient(ip);

            String CmdStop = this.gameDockerAPICMDRepo.findBycmdId("CmdStopStr").getCmd();
            String[] CmdStopStr =  this.dockerAPI.split_tap(CmdStop);
            Map<String,Boolean> startExecRequest = this.dockerAPI.makeExecStartInst();
            Map<String,Object> setStopInst = this.dockerAPI.makeExecInst(CmdStopStr);
            Mono<Map> AckStopStr = this.dockerAPI.makeExec(dockerId, setStopInst, this.dockerWebClient);
            String StopMeoStr = (String) AckStopStr.block().get("Id");
            Mono<String> AckStopEND = this.dockerAPI.startExec(StopMeoStr,startExecRequest, this.dockerWebClient);
            AckStopEND.block();

            String CmdStopAck = this.gameDockerAPICMDRepo.findBycmdId("CmdStopAckStr").getCmd();
            String[] CmdStopAckStr = this.dockerAPI.split_tap(CmdStopAck);
            Map<String,Object> setAckInst = this.dockerAPI.makeExecInst(CmdStopAckStr);
            Mono<Map> AckAckInfoStr = this.dockerAPI.makeExec(dockerId, setAckInst, this.dockerWebClient);
            String AckMeoStr = (String) AckAckInfoStr.block().get("Id");
            Mono<String> AckAckInfoEND = this.dockerAPI.startExec(AckMeoStr,startExecRequest, this.dockerWebClient);
            AckAckInfoEND.block();

            String Ack = (String)AckAckInfoEND.block();
            startGameServerDto.setIsWorking(Ack.equals("stopAck"));
            System.out.println(startGameServerDto.getIsWorking());
            return Mono.just(startGameServerDto);
        } catch (DoNotHaveServerException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("This user does not have an open server"));
        }
    }
}
