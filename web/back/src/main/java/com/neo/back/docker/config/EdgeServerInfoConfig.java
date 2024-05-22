package com.neo.back.docker.config;

import java.util.List;

import com.neo.back.docker.entity.MinecreftServerSetting;
import com.neo.back.docker.repository.GameServerSettingRepository;
import com.neo.back.docker.repository.GameTagRepository;
import com.neo.back.springjwt.dto.JoinDTO;
import com.neo.back.springjwt.entity.User;
import com.neo.back.springjwt.repository.UserRepository;
import com.neo.back.springjwt.service.JoinService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.neo.back.docker.entity.DockerServer;
import com.neo.back.docker.entity.EdgeServer;
import com.neo.back.docker.entity.Game;
import com.neo.back.docker.entity.GameDockerAPICMD;
import com.neo.back.docker.entity.GameTag;
import com.neo.back.docker.repository.DockerServerRepository;
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
    @Value("#{'${edgeservers.externalIp}'.split(',')}")private List<String> edgeServerExternalIp;
	@Value("#{'${edgeservers.user.id}'.split(',')}")private List<String> edgeServerUser;
	@Value("#{'${edgeservers.password}'.split(',')}")private List<String> edgeServerPassword;
	@Value("#{'${edgeservers.memoryTotal}'.split(',')}")private List<String> edgeServerMemoryTotal;
	@Value("#{'${edgeservers.memoryUse}'.split(',')}")private List<String> edgeServerMemoryUse;


    private final EdgeServerRepository edgeServerInfo;

    private final GameRepository gameRepo;

    private final GameServerSettingRepository gameServerSettingRepo;

    private final GameDockerAPICMDRepository gameDockerAPICMDRepo;

    private final DockerServerRepository dockerServerRepo;

    private final UserRepository UserRepo;

    private final JoinService joinService;

    private final GameTagRepository gameTagRepo;

	@PostConstruct
	private void init() {
        EdgeServer edgeServer = new EdgeServer();
        int edgeServerNumber = edgeServerIp.size();
		for(int index = 0; index < edgeServerNumber ; index++){
            edgeServer.setEdgeServerName(edgeServerName.get(index));
            edgeServer.setIp(edgeServerIp.get(index));
            edgeServer.setExternalIp(edgeServerExternalIp.get(index));
            edgeServer.setUser(edgeServerUser.get(index));
            edgeServer.setPassWord(edgeServerPassword.get(index));
            edgeServer.setMemoryTotal(Integer.parseInt(edgeServerMemoryTotal.get(index)));
            edgeServer.setMemoryUse(Integer.parseInt(edgeServerMemoryUse.get(index)));
            edgeServerInfo.save(edgeServer);
        }

        MinecreftServerSetting minecreftServerSetting = new MinecreftServerSetting();
        minecreftServerSetting.setSettingFilePath("/server/server.properties");
        gameServerSettingRepo.save(minecreftServerSetting);

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

        saveCMD(mine1_16_5, mine1_19_2, mine1_20_4);

        // User INFO
        User Sunwo = saveUser("SunWo","SunWo","Rmalen");
        User Jihoon = saveUser("JiHoon","JiHoon1!","Jiman");
        User Minseo = saveUser("Minseo","Minseo0O","Minseo");
        User Haeun = saveUser("Haeun","Haeun000111!","Haeun");
        User Seungmin = saveUser("Seungmin","Seungmin000111!","Seungmin");
        // 5 User
        User Jiwoo = saveUser("Jiwoo","SunJiwoo1!","Jiwoo");
        User Seoyeon = saveUser("Seoyeon","Seoyeon1!","Seoyeon");
        User Minjoon = saveUser("Minjoon","Minjoon0O","Minjoon");
        User Yujin = saveUser("Yujin","Yujin000111!","Yujin");
        User Jimin = saveUser("Jimin","Jimin000111!","Jimin");
        // 10

        saveGameTag("포켓몬");
        saveGameTag("모드");
        saveGameTag("모드팩");
        saveGameTag("미니게임");
        saveGameTag("농사");
        saveGameTag("건축");
        saveGameTag("크리에이티브 건축");
        saveGameTag("경제");
        saveGameTag("현실경제");
        saveGameTag("마인팜");
        saveGameTag("RPG");
        saveGameTag("PVP");
        saveGameTag("서바이벌");
        saveGameTag("좀비");
        saveGameTag("쿠키런");
        saveGameTag("마을");
        saveGameTag("요리");
        saveGameTag("파쿠르");
        saveGameTag("성인서버");
        saveGameTag("야생");
        saveGameTag("반야생");
        saveGameTag("스카이블럭");
        saveGameTag("개미굴");
        saveGameTag("mcMMO");
        saveGameTag("하드코어");
        saveGameTag("총기");
        saveGameTag("마법");

	}

    private void saveGameTag(String tag) {
        GameTag game = new GameTag();
        game.setTag(tag);
        gameTagRepo.save(game);
    }

    private void saveCMD(Game mine1_16_5, Game mine1_19_2, Game mine1_20_4) {
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
        CmdStartAckStr.setCmd("sh\t-c\ttimeout 5m tail -n 5 -f /control/output.txt | { flag=0; while IFS= read -r line; do if [[ \"$line\" == *\"Done\"* ]]; then echo \"startAck\"; pkill -P $$ tail; flag=1; break; fi; done; if [ $flag -eq 0 ]; then echo \"startERR\"; fi; }");
        CmdStartAckStr.setCmdId("CmdStartAckStr");
        
        GameDockerAPICMD CmdStopStr = new GameDockerAPICMD();
        CmdStopStr.setCmd("sh\t-c\techo 'input stop' > /control/input.txt");
        CmdStopStr.setCmdId("CmdStopStr");

        GameDockerAPICMD CmdStopAckStr = new GameDockerAPICMD();
        CmdStopAckStr.setCmd("sh\t-c\ttimeout 5m tail -n 5 -f /control/output.txt | { flag=0; while IFS= read -r line; do if [[ \"$line\" == *\"Saving worlds\"* ]]; then echo \"stopAck\" ; sleep 5 ; pkill -P $$ tail ; flag=1 ; break ; fi ; done ; if [ $flag -eq 0 ]; then echo \"stopERR\" ; fi ;}");
        CmdStopAckStr.setCmdId("CmdStopAckStr");

        GameDockerAPICMD makeDirStr = new GameDockerAPICMD();
        makeDirStr.setCmd("mkdir\tserver/");
        makeDirStr.setCmdId("makeDirStr");

        GameDockerAPICMD delMeoStr = new GameDockerAPICMD();
        delMeoStr.setCmd("rm\t-rf\tserver/");
        delMeoStr.setCmdId("delMeoStr");

        GameDockerAPICMD gameLog = new GameDockerAPICMD();
        gameLog.setCmd("sh\t-c\tcat control/output.txt");
        gameLog.setCmdId("gameLog");

        gameDockerAPICMDRepo.save(CmdStartStr);
        gameDockerAPICMDRepo.save(CmdStartAckStr);
        gameDockerAPICMDRepo.save(CmdStopStr);
        gameDockerAPICMDRepo.save(CmdStopAckStr);
        gameDockerAPICMDRepo.save(makeDirStr);
        gameDockerAPICMDRepo.save(delMeoStr);
        gameDockerAPICMDRepo.save(gameLog);
    }


    // private void saveDocker(
    //                 Game game, 
    //                 String ServerName, 
    //                 EdgeServer edge,
    //                 int port, 
    //                 String DockerId, 
    //                 int Ram,
    //                 String ServerComment, 
    //                 Boolean Public,
    //                 Boolean FreeAccess,
    //                 User serverUser, 
    //                 User user1, User user2, User user3,
    //                 User user4, User user5, User user6, User user7,
    //                 String tag1, String tag2, String tag3 ) {
    //     DockerServer docker = new DockerServer();
    //     docker.setGame(game);
    //     docker.setServerName(ServerName);
    //     docker.setUser(serverUser);
    //     docker.setBaseImage(null);
    //     docker.setEdgeServer(edge);
    //     docker.setPort(port); 
    //     docker.setDockerId(DockerId); 
    //     docker.setRAMCapacity(Ram); 
    //     docker.setServerComment(ServerComment);
    //     docker.setPublic(Public);
    //     docker.setFreeAccess(FreeAccess);

    //     docker.addApplicant(user1);
    //     docker.addApplicant(user2);
    //     docker.addApplicant(user3);

    //     docker.addParticipant(user4);
    //     docker.addParticipant(user5);
    //     docker.addParticipant(user6);
    //     docker.addParticipant(user7);
        
    //     docker.addGameTag(gameTagRepo.findByTag(tag1));
    //     docker.addGameTag(gameTagRepo.findByTag(tag2));
    //     docker.addGameTag(gameTagRepo.findByTag(tag3));
    //     dockerServerRepo.save(docker);
    // }


    private User saveUser(String Username,String password,String name) {
        JoinDTO joinDTO = new JoinDTO();
        joinDTO.setUsername(Username);
        joinDTO.setPassword(password);
        joinService.joinProcess(joinDTO);

        User user = UserRepo.findByUsername(Username);

        // user.setName(name);
        // user.setPassword(password);
        // user.setUsername(Username);
        // UserRepo.save(user);
        return user;
    }
}
