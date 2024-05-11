package com.neo.back.docker.config;

import java.util.ArrayList;
import java.util.List;

import com.neo.back.docker.entity.MinecreftServerSetting;
import com.neo.back.docker.repository.GameServerSettingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.neo.back.docker.entity.EdgeServer;
import com.neo.back.docker.entity.Game;
import com.neo.back.docker.entity.GameDockerAPICMD;
import com.neo.back.docker.repository.EdgeServerRepository;
import com.neo.back.docker.repository.GameDockerAPICMDRepository;
import com.neo.back.docker.repository.GameRepository;

import jakarta.annotation.PostConstruct;

@Configuration
@Transactional
@RequiredArgsConstructor
public class EdgeServerInfoConfig {
    
	@Value("#{'${edgeservers.id}'.split(',')}")private List<String> edgeServerName;
    @Value("#{'${edgeservers.ip}'.split(',')}")private List<String> edgeServerIp;
	@Value("#{'${edgeservers.user.id}'.split(',')}")private List<String> edgeServerUser;
	@Value("#{'${edgeservers.password}'.split(',')}")private List<String> edgeServerPassword;
	@Value("#{'${edgeservers.memoryTotal}'.split(',')}")private List<String> edgeServerMemoryTotal;
	@Value("#{'${edgeservers.memoryUse}'.split(',')}")private List<String> edgeServerMemoryUse;


    private final EdgeServerRepository edgeServerInfo;

    private final GameRepository gameRepo;

    private final GameServerSettingRepository gameServerSettingRepo;

    private final GameDockerAPICMDRepository gameDockerAPICMDRepo;

	@PostConstruct
	private void init() {
		EdgeServer edgeServer = new EdgeServer();
        int edgeServerNumber = edgeServerIp.size();
		for(int index = 0; index < edgeServerNumber ; index++){
            edgeServer.setEdgeServerName(edgeServerName.get(index));
            edgeServer.setIp(edgeServerIp.get(index));
            edgeServer.setUser(edgeServerUser.get(index));
            edgeServer.setPassWord(edgeServerPassword.get(index));
            edgeServer.setMemoryTotal(Integer.parseInt(edgeServerMemoryTotal.get(index)));
            edgeServer.setMemoryUse(Integer.parseInt(edgeServerMemoryUse.get(index)));
            edgeServerInfo.save(edgeServer);
        }

        MinecreftServerSetting minecreftServerSetting = new MinecreftServerSetting();
        minecreftServerSetting.setSettingFilePath("/server/server.properties");
        gameServerSettingRepo.save(minecreftServerSetting);

        // String[] CmdMemoryStr = new String[]{"sh","-c","echo 'java,-Xmx" + memory + "G,-jar,/server/craftbukkit-1.20.4.jar' >  /control/meomory.txt"};
        
        // "echo 'java,-Xms1G,-Xmx" + memory + "G,-XX:+IgnoreUnrecognizedVMOptions,-XX:+UseG1GC,-XX:+ParallelRefProcEnabled,-XX:MaxGCPauseMillis=200,-XX:+UnlockExperimentalVMOptions,-XX:+DisableExplicitGC,-XX:+AlwaysPreTouch,-XX:G1HeapWastePercent=5,-XX:G1MixedGCCountTarget=4,-XX:G1MixedGCLiveThresholdPercent=90,-XX:G1RSetUpdatingPauseTimePercent=5,-XX:SurvivorRatio=32,-XX:+PerfDisableSharedMem,-XX:MaxTenuringThreshold=1,-XX:G1NewSizePercent=30,-XX:G1MaxNewSizePercent=40,-XX:G1HeapRegionSize=8M,-XX:G1ReservePercent=20,-XX:InitiatingHeapOccupancyPercent=15,-Dusing.aikars.flags=https://mcflags.emc.gs,-Daikars.new.flags=true,-jar,/server/craftbukkit-1.20.4.jar,nogui' >  /control/meomory.txt"
        // String[] CmdStartStr = new String[]{"sh","-c","echo 'start' > /control/input.txt"};
        // String[] CmdStartAckStr = new String[]{"sh","-c","/control/start.sh"};

        // String[] CmdStopStr =  new String[]{"sh","-c","echo 'input stop' > /control/input.txt"};
        // String[] CmdStopAckStr = new String[]{"sh","-c","/control/stop.sh"};
        Game mine1_16_5 = new Game();
        mine1_16_5.setGameName("Minecraft");
        mine1_16_5.setVersion("1.16.5");
        mine1_16_5.setDockerImage("mc1.16.5");
        mine1_16_5.setDefaultSetting(minecreftServerSetting);
        gameRepo.save(mine1_16_5);

        Game mine1_19_2 = new Game();
        mine1_19_2.setGameName("Minecraft");
        mine1_19_2.setVersion("1.19.2");
        mine1_19_2.setDockerImage("mc1.19.2");
        mine1_19_2.setDefaultSetting(minecreftServerSetting);
        gameRepo.save(mine1_19_2);

        Game mine1_20_4 = new Game();
        mine1_20_4.setGameName("Minecraft");
        mine1_20_4.setVersion("1.20.4");
        mine1_20_4.setDockerImage("mc1.20.4");
        mine1_20_4.setDefaultSetting(minecreftServerSetting);
        gameRepo.save(mine1_20_4);

        GameDockerAPICMD CmdMemory_Mine_1_16_5Str = new GameDockerAPICMD();
        CmdMemory_Mine_1_16_5Str.setCmd("sh\t-c\techo 'java,-Xms1G,-XmxMEMORYG,-XX:+IgnoreUnrecognizedVMOptions,-XX:+UseG1GC,-XX:+ParallelRefProcEnabled,-XX:MaxGCPauseMillis=200,-XX:+UnlockExperimentalVMOptions,-XX:+DisableExplicitGC,-XX:+AlwaysPreTouch,-XX:G1HeapWastePercent=5,-XX:G1MixedGCCountTarget=4,-XX:G1MixedGCLiveThresholdPercent=90,-XX:G1RSetUpdatingPauseTimePercent=5,-XX:SurvivorRatio=32,-XX:+PerfDisableSharedMem,-XX:MaxTenuringThreshold=1,-XX:G1NewSizePercent=30,-XX:G1MaxNewSizePercent=40,-XX:G1HeapRegionSize=8M,-XX:G1ReservePercent=20,-XX:InitiatingHeapOccupancyPercent=15,-Dusing.aikars.flags=https://mcflags.emc.gs,-Daikars.new.flags=true,-jar,/server/craftbukkit-1.16.5.jar,nogui' >  /control/meomory.txt");
        CmdMemory_Mine_1_16_5Str.setCmdId("1.16.5_START");
        CmdMemory_Mine_1_16_5Str.setGame(mine1_16_5);

        GameDockerAPICMD CmdMemory_Mine_1_19_2Str = new GameDockerAPICMD();
        CmdMemory_Mine_1_19_2Str.setCmd("sh\t-c\techo 'java,-Xms1G,-XmxMEMORYG,-XX:+IgnoreUnrecognizedVMOptions,-XX:+UseG1GC,-XX:+ParallelRefProcEnabled,-XX:MaxGCPauseMillis=200,-XX:+UnlockExperimentalVMOptions,-XX:+DisableExplicitGC,-XX:+AlwaysPreTouch,-XX:G1HeapWastePercent=5,-XX:G1MixedGCCountTarget=4,-XX:G1MixedGCLiveThresholdPercent=90,-XX:G1RSetUpdatingPauseTimePercent=5,-XX:SurvivorRatio=32,-XX:+PerfDisableSharedMem,-XX:MaxTenuringThreshold=1,-XX:G1NewSizePercent=30,-XX:G1MaxNewSizePercent=40,-XX:G1HeapRegionSize=8M,-XX:G1ReservePercent=20,-XX:InitiatingHeapOccupancyPercent=15,-Dusing.aikars.flags=https://mcflags.emc.gs,-Daikars.new.flags=true,-jar,/server/craftbukkit-1.19.2.jar,nogui' >  /control/meomory.txt");
        CmdMemory_Mine_1_19_2Str.setCmdId("1.19.2_START");
        CmdMemory_Mine_1_19_2Str.setGame(mine1_19_2);
        
        GameDockerAPICMD CmdMemory_Mine_1_20_4Str = new GameDockerAPICMD();
        CmdMemory_Mine_1_20_4Str.setCmd("sh\t-c\techo 'java,-Xms1G,-XmxMEMORYG,-XX:+IgnoreUnrecognizedVMOptions,-XX:+UseG1GC,-XX:+ParallelRefProcEnabled,-XX:MaxGCPauseMillis=200,-XX:+UnlockExperimentalVMOptions,-XX:+DisableExplicitGC,-XX:+AlwaysPreTouch,-XX:G1HeapWastePercent=5,-XX:G1MixedGCCountTarget=4,-XX:G1MixedGCLiveThresholdPercent=90,-XX:G1RSetUpdatingPauseTimePercent=5,-XX:SurvivorRatio=32,-XX:+PerfDisableSharedMem,-XX:MaxTenuringThreshold=1,-XX:G1NewSizePercent=30,-XX:G1MaxNewSizePercent=40,-XX:G1HeapRegionSize=8M,-XX:G1ReservePercent=20,-XX:InitiatingHeapOccupancyPercent=15,-Dusing.aikars.flags=https://mcflags.emc.gs,-Daikars.new.flags=true,-jar,/server/craftbukkit-1.20.4.jar,nogui' >  /control/meomory.txt");
        CmdMemory_Mine_1_20_4Str.setCmdId("1.20.4_START");
        CmdMemory_Mine_1_20_4Str.setGame(mine1_20_4);

        gameDockerAPICMDRepo.save(CmdMemory_Mine_1_16_5Str);
        gameDockerAPICMDRepo.save(CmdMemory_Mine_1_19_2Str);
        gameDockerAPICMDRepo.save(CmdMemory_Mine_1_20_4Str);

        GameDockerAPICMD CmdStartStr = new GameDockerAPICMD();
        CmdStartStr.setCmd("sh\t-c\techo 'start' > /control/input.txt");
        CmdStartStr.setCmdId("CmdStartStr");
        
        GameDockerAPICMD CmdStartAckStr = new GameDockerAPICMD();
        CmdStartAckStr.setCmd("sh\t-c\t/control/start.sh");
        CmdStartAckStr.setCmdId("CmdStartAckStr");
        
        GameDockerAPICMD CmdStopStr = new GameDockerAPICMD();
        CmdStopStr.setCmd("sh\t-c\techo 'input stop' > /control/input.txt");
        CmdStopStr.setCmdId("CmdStopStr");

        GameDockerAPICMD CmdStopAckStr = new GameDockerAPICMD();
        CmdStopAckStr.setCmd("sh\t-c\t/control/stop.sh");
        CmdStopAckStr.setCmdId("CmdStopAckStr");

        GameDockerAPICMD makeDirStr = new GameDockerAPICMD();
        makeDirStr.setCmd("mkdir\tserver/");
        makeDirStr.setCmdId("makeDirStr");

        GameDockerAPICMD delMeoStr = new GameDockerAPICMD();
        delMeoStr.setCmd("rm\t-rf\tserver/");
        delMeoStr.setCmdId("delMeoStr");

        gameDockerAPICMDRepo.save(CmdStartStr);
        gameDockerAPICMDRepo.save(CmdStartAckStr);
        gameDockerAPICMDRepo.save(CmdStopStr);
        gameDockerAPICMDRepo.save(CmdStopAckStr);
        gameDockerAPICMDRepo.save(makeDirStr);
        gameDockerAPICMDRepo.save(delMeoStr);

	}
}
