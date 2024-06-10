package com.neo.back.mainService.config;

import java.util.List;
import java.util.Optional;

import com.neo.back.mainService.entity.MinecreftServerSetting;
import com.neo.back.mainService.repository.GameServerSettingRepository;
import com.neo.back.mainService.repository.GameTagRepository;
import com.neo.back.authorization.dto.JoinDTO;
import com.neo.back.authorization.entity.User;
import com.neo.back.authorization.repository.UserRepository;
import com.neo.back.authorization.service.JoinService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.neo.back.mainService.entity.DockerServer;
import com.neo.back.mainService.entity.EdgeServer;
import com.neo.back.mainService.entity.Game;
import com.neo.back.mainService.entity.GameDockerAPICMD;
import com.neo.back.mainService.repository.DockerServerRepository;
import com.neo.back.mainService.repository.EdgeServerRepository;
import com.neo.back.mainService.repository.GameDockerAPICMDRepository;
import com.neo.back.mainService.repository.GameRepository;

import jakarta.annotation.PostConstruct;

@Configuration
@Transactional
@RequiredArgsConstructor
public class EdgeServerInfoConfig {

    @Value("#{'${edgeservers.id}'.split(',')}")
    private List<String> edgeServerName;
    @Value("#{'${edgeservers.ip}'.split(',')}")
    private List<String> edgeServerIp;
    @Value("#{'${edgeservers.externalIp}'.split(',')}")
    private List<String> edgeServerExternalIp;
    @Value("#{'${edgeservers.user.id}'.split(',')}")
    private List<String> edgeServerUser;
    @Value("#{'${edgeservers.password}'.split(',')}")
    private List<String> edgeServerPassword;
    @Value("#{'${edgeservers.memoryTotal}'.split(',')}")
    private List<String> edgeServerMemoryTotal;
    @Value("#{'${edgeservers.memoryUse}'.split(',')}")
    private List<String> edgeServerMemoryUse;


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
        for (int index = 0; index < edgeServerNumber; index++) {
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

       Game mine1_16_5 = new Game(
           "Minecraft",
           "1.16.5",
           "mc1.16.5",
           "25565/tcp",
           "/server",
           "/server.properties",
           "\n",
           "=",
           "max-players"
       );

       Game mine1_19_2 = new Game(
           "Minecraft",
           "1.19.2",
           "mc1.19.2",
           "25565/tcp",
           "/server",
           "/server.properties",
           "\n",
           "=",
           "max-players"
       );

       Game mine1_20_4 = new Game(
           "Minecraft",
           "1.20.4",
           "mc1.20.4",
           "25565/tcp",
           "/server",
           "/server.properties",
           "\n",
           "=",
           "max-players"
       );

       Game palworld = new Game(
           "Palworld",
           null,
           "palworld",
           "8211/udp",
           "/home/steam/Steam/steamapps/common/PalServer/Pal/Saved/Config/LinuxServer",
           "/PalWorldSettings.ini",
           ",",
           "=",
           "ServerPlayerMaxNum"
       );

       Game terraria = new Game(
           "Terraria",
           null,
           "terraria",
           "7777/tcp",
           "/config",
           "/serverconfig.txt",
           "",
           "",
           ""
       );

       gameRepo.save(mine1_16_5);
       gameRepo.save(mine1_19_2);
       gameRepo.save(mine1_20_4);
       gameRepo.save(palworld);

       saveCMD_common();
       saveCMD_mine(mine1_16_5, mine1_19_2, mine1_20_4,palworld,terraria);

        User root = saveUser("root@naver.com", "root", "root", true);

        // User INFO
        User Sunwo = saveUser("sunwo@naver.com", "sunwo", "malenwater", false);
        User Jihoon = saveUser("misu@naver.com", "misu", "Jiman_misu", false);
        User Minseo = saveUser("Minseo@naver.com", "Minseo0O", "Minseo", false);
        User Haeun = saveUser("Haeun@naver.com", "Haeun000111!", "Haeun", false);
        User Seungmin = saveUser("Seungmin@naver.com", "Seungmin000111!", "Seungmin", false);
        // 5 User
        User Jiwoo = saveUser("Jiwoo@naver.com", "SunJiwoo1!", "Jiwoo", false);
        User Seoyeon = saveUser("Seoyeon@naver.com", "Seoyeon1!", "Seoyeon", false);
        User Minjoon = saveUser("Minjoon@naver.com", "Minjoon0O", "Minjoon", false);
        User Yujin = saveUser("Yujin@naver.com", "Yujin000111!", "Yujin", false);
        User Jimin = saveUser("Jimin@naver.com", "Jimin000111!", "Jimin", false);
        // 10

       // saveGameTag("포켓몬");
       // saveGameTag("모드");
       // saveGameTag("모드팩");
       // saveGameTag("미니게임");
       // saveGameTag("농사");
       // saveGameTag("건축");
       // saveGameTag("크리에이티브 건축");
       // saveGameTag("경제");
       // saveGameTag("현실경제");
       // saveGameTag("마인팜");
       // saveGameTag("RPG");
       // saveGameTag("PVP");
       // saveGameTag("서바이벌");
       // saveGameTag("좀비");
       // saveGameTag("쿠키런");
       // saveGameTag("마을");
       // saveGameTag("요리");
       // saveGameTag("파쿠르");
       // saveGameTag("성인서버");
       // saveGameTag("야생");
       // saveGameTag("반야생");
       // saveGameTag("스카이블럭");
       // saveGameTag("개미굴");
       // saveGameTag("mcMMO");
       // saveGameTag("하드코어");
       // saveGameTag("총기");
       // saveGameTag("마법");

    //    this.saveDocker(gameRepo.findById((long) 3).orElse(null),
    //    "선우의 서버",
    //    edgeServerInfo.findByEdgeServerName("edgeServer_1"),
    //    43556,
    //    "????",
    //    4 ,
    //    "놀러와요. 선우의 숲",
    //    true,
    //    true,
    //    Sunwo,
    //    Jihoon, Minseo, Seungmin,
    //    Seoyeon, Minjoon, Yujin, Jimin,
    //    "좀비","성인서버","반야생");

       // this.saveDocker(gameRepo.findById((long) 1).orElse(null),
       // "Yujin Server",
       // edgeServerInfo.findByEdgeServerName("edgeServer_1"),
       // 66781,
       // "jlkasdasdjasdfppjlj213412",
       // 4,
       // "놀러와요. Yujin의 숲",
       // true,
       // true,
       // Yujin,
       // Sunwo, Jimin, Minjoon,
       // Haeun, Minseo, Jiwoo, Seoyeon,
       // "경제","마인팜","서바이벌");
        //상품저장
        //bronze,silver,gold,diamond


        // this.saveDocker(gameRepo.findById((long) 3).orElse(null), 
        // "선우의 서버",
        // edgeServerInfo.findByEdgeServerName("edgeServer_1"),
        // 43556,
        // "????", 
        // 4 ,
        // "놀러와요. 선우의 숲",
        // true,
        // true,
        // Sunwo,
        // Jihoon, Minseo, Seungmin,
        // Seoyeon, Minjoon, Yujin, Jimin,
        // "좀비","성인서버","반야생");

        // this.saveDocker(gameRepo.findById((long) 1).orElse(null),
        // "Yujin Server",
        // edgeServerInfo.findByEdgeServerName("edgeServer_1"),
        // 66781,
        // "jlkasdasdjasdfppjlj213412",
        // 4,
        // "놀러와요. Yujin의 숲",
        // true, 
        // true,
        // Yujin,
        // Sunwo, Jimin, Minjoon,
        // Haeun, Minseo, Jiwoo, Seoyeon,
        // "경제","마인팜","서바이벌");

       // this.saveDocker(gameRepo.findById((long) 1).orElse(null),
       // "Jihoon Server",
       // edgeServerInfo.findByEdgeServerName("edgeServer_1"),
       // 88888,
       // "51ssfsafasfafsdfppjlj213412",
       // 4,
       // "놀러와요. Jihoon의 숲",
       // true,
       // true,
       // Jihoon,
       // Minseo, Seungmin, Jiwoo,
       // Sunwo, Haeun, Minjoon, Yujin,
       // "모드","미니게임","건축");

    }

   // private void saveGameTag(String tag) {
   //     GameTag game = new GameTag();
   //     game.setTag(tag);
   //     gameTagRepo.save(game);
   // }

    private void saveCMD_common() {
        saveIfNotExists("sh\t-c\techo 'start' > /control/input.txt", "CmdStartStr", "common");
        saveIfNotExists("sh\t-c\tcat /control/output.txt", "gameLog", "common");
        saveIfNotExists("sh\t-c\techo 'input INPUT' > /control/input.txt", "input", "common");
    }

    private void saveIfNotExists(String cmd, String cmdId, String cmdKind) {
        Optional<GameDockerAPICMD> existingCmd = Optional.ofNullable(gameDockerAPICMDRepo.findBycmdId(cmdId));
        if (existingCmd.isPresent()) {
            System.out.println("Duplicate entry detected for cmdId: " + cmdId);
        } else {
            GameDockerAPICMD newCmd = new GameDockerAPICMD();
            newCmd.setCmd(cmd);
            newCmd.setCmdId(cmdId);
            newCmd.setCmdKind(cmdKind);
            gameDockerAPICMDRepo.save(newCmd);
        }
    }

    private void saveCMD_mine(Game mine1_16_5, Game mine1_19_2, Game mine1_20_4, Game palworld, Game terraria) {
        GameDockerAPICMD CmdMemory_Mine_1_16_5Str = new GameDockerAPICMD();
        CmdMemory_Mine_1_16_5Str.setCmd("sh\t-c\techo 'java,-Xms1G,-XmxMEMORYG,-XX:+IgnoreUnrecognizedVMOptions,-XX:+UseG1GC,-XX:+ParallelRefProcEnabled,-XX:MaxGCPauseMillis=200,-XX:+UnlockExperimentalVMOptions,-XX:+DisableExplicitGC,-XX:+AlwaysPreTouch,-XX:G1HeapWastePercent=5,-XX:G1MixedGCCountTarget=4,-XX:G1MixedGCLiveThresholdPercent=90,-XX:G1RSetUpdatingPauseTimePercent=5,-XX:SurvivorRatio=32,-XX:+PerfDisableSharedMem,-XX:MaxTenuringThreshold=1,-XX:G1NewSizePercent=30,-XX:G1MaxNewSizePercent=40,-XX:G1HeapRegionSize=8M,-XX:G1ReservePercent=20,-XX:InitiatingHeapOccupancyPercent=15,-Dusing.aikars.flags=https://mcflags.emc.gs,-Daikars.new.flags=true,-jar,/server/craftbukkit-1.16.5.jar,nogui' >  /control/meomory.txt");
        CmdMemory_Mine_1_16_5Str.setCmdId("1.16.5_START");
        CmdMemory_Mine_1_16_5Str.setCmdKind("execCMD");

       GameDockerAPICMD CmdMemory_Mine_1_19_2Str = new GameDockerAPICMD();
       CmdMemory_Mine_1_19_2Str.setCmd("sh\t-c\techo 'java,-Xms1G,-XmxMEMORYG,-XX:+IgnoreUnrecognizedVMOptions,-XX:+UseG1GC,-XX:+ParallelRefProcEnabled,-XX:MaxGCPauseMillis=200,-XX:+UnlockExperimentalVMOptions,-XX:+DisableExplicitGC,-XX:+AlwaysPreTouch,-XX:G1HeapWastePercent=5,-XX:G1MixedGCCountTarget=4,-XX:G1MixedGCLiveThresholdPercent=90,-XX:G1RSetUpdatingPauseTimePercent=5,-XX:SurvivorRatio=32,-XX:+PerfDisableSharedMem,-XX:MaxTenuringThreshold=1,-XX:G1NewSizePercent=30,-XX:G1MaxNewSizePercent=40,-XX:G1HeapRegionSize=8M,-XX:G1ReservePercent=20,-XX:InitiatingHeapOccupancyPercent=15,-Dusing.aikars.flags=https://mcflags.emc.gs,-Daikars.new.flags=true,-jar,/server/craftbukkit-1.19.2.jar,nogui' >  /control/meomory.txt");
       CmdMemory_Mine_1_19_2Str.setCmdId("1.19.2_START");
       CmdMemory_Mine_1_19_2Str.setCmdKind("execCMD");

       GameDockerAPICMD CmdMemory_Mine_1_20_4Str = new GameDockerAPICMD();
       CmdMemory_Mine_1_20_4Str.setCmd("sh\t-c\techo 'java,-Xms1G,-XmxMEMORYG,-XX:+IgnoreUnrecognizedVMOptions,-XX:+UseG1GC,-XX:+ParallelRefProcEnabled,-XX:MaxGCPauseMillis=200,-XX:+UnlockExperimentalVMOptions,-XX:+DisableExplicitGC,-XX:+AlwaysPreTouch,-XX:G1HeapWastePercent=5,-XX:G1MixedGCCountTarget=4,-XX:G1MixedGCLiveThresholdPercent=90,-XX:G1RSetUpdatingPauseTimePercent=5,-XX:SurvivorRatio=32,-XX:+PerfDisableSharedMem,-XX:MaxTenuringThreshold=1,-XX:G1NewSizePercent=30,-XX:G1MaxNewSizePercent=40,-XX:G1HeapRegionSize=8M,-XX:G1ReservePercent=20,-XX:InitiatingHeapOccupancyPercent=15,-Dusing.aikars.flags=https://mcflags.emc.gs,-Daikars.new.flags=true,-jar,/server/craftbukkit-1.20.4.jar,nogui' >  /control/meomory.txt");
       CmdMemory_Mine_1_20_4Str.setCmdId("1.20.4_START");
       CmdMemory_Mine_1_20_4Str.setCmdKind("execCMD");

       GameDockerAPICMD CmdMemory_palworld = new GameDockerAPICMD();
       CmdMemory_palworld.setCmd("bash\t-c\techo \"/control/palStart.sh\" >  /control/meomory.txt");
       CmdMemory_palworld.setCmdId("CmdMemory_palworld");
       CmdMemory_palworld.setCmdKind("execCMD");

       GameDockerAPICMD CmdMemory_terraria = new GameDockerAPICMD();
       CmdMemory_terraria.setCmd("bash\t-c\techo \"./run.sh\" >  /control/meomory.txt");
       CmdMemory_terraria.setCmdId("CmdMemory_terraria");
       CmdMemory_terraria.setCmdKind("execCMD");

       gameDockerAPICMDRepo.save(CmdMemory_Mine_1_16_5Str);
       gameDockerAPICMDRepo.save(CmdMemory_Mine_1_19_2Str);
       gameDockerAPICMDRepo.save(CmdMemory_Mine_1_20_4Str);
       gameDockerAPICMDRepo.save(CmdMemory_palworld);
       gameDockerAPICMDRepo.save(CmdMemory_terraria);

       GameDockerAPICMD CmdStartAckStr_mine = new GameDockerAPICMD();
       CmdStartAckStr_mine.setCmd("sh\t-c\ttimeout 5m tail -n 5 -f /control/output.txt | { flag=0; while IFS= read -r line; do if [[ \"$line\" == *\"Done\"* ]]; then echo \"startAck\"; pkill -P $$ tail; flag=1; break; fi; done; if [ $flag -eq 0 ]; then echo \"startERR\"; fi; }");
       CmdStartAckStr_mine.setCmdId("CmdStartAckStr");
       CmdStartAckStr_mine.setCmdKind("start_ack");

       GameDockerAPICMD CmdStopAckStr_mine = new GameDockerAPICMD();
       CmdStopAckStr_mine.setCmd("sh\t-c\ttimeout 5m tail -n 5 -f /control/output.txt | { flag=0; while IFS= read -r line; do if [[ \"$line\" == *\"Saving worlds\"* ]]; then echo \"stopAck\" ; sleep 5 ; pkill -P $$ tail ; flag=1 ; break ; fi ; done ; if [ $flag -eq 0 ]; then echo \"stopERR\" ; fi ;}");
       CmdStopAckStr_mine.setCmdId("CmdStopAckStr");
       CmdStopAckStr_mine.setCmdKind("stop_ack");

       GameDockerAPICMD banlist_mine = new GameDockerAPICMD();
       banlist_mine.setCmd("sh\t-c\t[ -s server/banned-players.json ] && cat server/banned-players.json || echo \"null\"");
       banlist_mine.setCmdId("banlist");
       banlist_mine.setCmdKind("userBan");

       GameDockerAPICMD running_mine = new GameDockerAPICMD();
       running_mine.setCmd("sh\t-c\tps | grep /server/craftbukkit-");
       running_mine.setCmdId("running_mine");
       running_mine.setCmdKind("serverRun");

       GameDockerAPICMD UserListcmd_mine = new GameDockerAPICMD();
       UserListcmd_mine.setCmd("sh\t-c\techo '[Server thread/INFO]: , joined the game,[Server thread/INFO]: , left the game' > /control/user_cmd.txt");
       UserListcmd_mine.setCmdId("userListCMD_mine");
       UserListcmd_mine.setCmdKind("userListCMD");

       GameDockerAPICMD UserListcmd_mine_1_16_5 = new GameDockerAPICMD();
       UserListcmd_mine_1_16_5.setCmd("sh\t-c\techo '[Server thread/INFO]: ,[/,[Server thread/INFO]: , left the game' > control/user_cmd.txt");
       UserListcmd_mine_1_16_5.setCmdId("UserListcmd_mine_1_16_5");
       UserListcmd_mine_1_16_5.setCmdKind("userListCMD");

       GameDockerAPICMD UserList_mine = new GameDockerAPICMD();
       UserList_mine.setCmd("sh\t-c\t[ -s /control/user.txt ] && cat /control/user.txt || echo \"null\"");
       UserList_mine.setCmdId("userList_mine");
       UserList_mine.setCmdKind("userList");

       GameDockerAPICMD pathFolder_mine = new GameDockerAPICMD();
       pathFolder_mine.setCmd("sh\t-c\techo \"/server/\" > /control/dataPath.txt");
       pathFolder_mine.setCmdId("pathFolder_mine");
       pathFolder_mine.setCmdKind("pathFolder");

       GameDockerAPICMD pathFileList_mine = new GameDockerAPICMD();
       pathFileList_mine.setCmd("/server/");
       pathFileList_mine.setCmdId("pathFileList_mine");
       pathFileList_mine.setCmdKind("pathFileList");

       GameDockerAPICMD makeDirStr_mine = new GameDockerAPICMD();
       makeDirStr_mine.setCmd("mkdir\tserver/");
       makeDirStr_mine.setCmdId("makeDirStr_mine");
       makeDirStr_mine.setCmdKind("makeDirStr");

       GameDockerAPICMD delMeoStr_mine = new GameDockerAPICMD();
       delMeoStr_mine.setCmd("rm\t-rf\tserver/");
       delMeoStr_mine.setCmdId("delMeoStr_mine");
       delMeoStr_mine.setCmdKind("delMeoStr");

       GameDockerAPICMD CmdStopStr_mine = new GameDockerAPICMD();
       CmdStopStr_mine.setCmd("sh\t-c\techo 'input stop' > /control/input.txt");
       CmdStopStr_mine.setCmdId("CmdStopStr_mine");
       CmdStopStr_mine.setCmdKind("CmdStopStr");

       GameDockerAPICMD SearchStr_mine = new GameDockerAPICMD();
       SearchStr_mine.setCmd("/server/craftbukkit-");
       SearchStr_mine.setCmdId("SearchStr_mine");
       SearchStr_mine.setCmdKind("SearchStr");

       gameDockerAPICMDRepo.save(CmdStopStr_mine);
       gameDockerAPICMDRepo.save(CmdStartAckStr_mine);
       gameDockerAPICMDRepo.save(CmdStopAckStr_mine);
       gameDockerAPICMDRepo.save(banlist_mine);
       gameDockerAPICMDRepo.save(running_mine);
       gameDockerAPICMDRepo.save(UserListcmd_mine);
       gameDockerAPICMDRepo.save(UserListcmd_mine_1_16_5);
       gameDockerAPICMDRepo.save(UserList_mine);
       gameDockerAPICMDRepo.save(pathFolder_mine);
       gameDockerAPICMDRepo.save(pathFileList_mine);
       gameDockerAPICMDRepo.save(SearchStr_mine);
       gameDockerAPICMDRepo.save(makeDirStr_mine);
       gameDockerAPICMDRepo.save(delMeoStr_mine);

       GameDockerAPICMD pathFolder_pal = new GameDockerAPICMD();
       pathFolder_pal.setCmd("bash\t-c\techo \"/\" > /control/dataPath.txt");
       pathFolder_pal.setCmdId("pathFolder_pal");
       pathFolder_pal.setCmdKind("pathFolder");

       GameDockerAPICMD makeDirStr_pal = new GameDockerAPICMD();
       makeDirStr_pal.setCmd("mkdir\t/home/steam/Steam/steamapps/common/PalServer/");
       makeDirStr_pal.setCmdId("makeDirStr_pal");
       makeDirStr_pal.setCmdKind("makeDirStr");

       GameDockerAPICMD delMeoStr_pal = new GameDockerAPICMD();
       delMeoStr_pal.setCmd("rm\t-rf\t/home/steam/Steam/steamapps/common/PalServer/");
       delMeoStr_pal.setCmdId("delMeoStr_pal");
       delMeoStr_pal.setCmdKind("delMeoStr");

       GameDockerAPICMD pathFileList_pal = new GameDockerAPICMD();
       pathFileList_pal.setCmd("/home/steam/Steam/steamapps/common/PalServer/");
       pathFileList_pal.setCmdId("pathFileList_pal");
       pathFileList_pal.setCmdKind("pathFileList");

       GameDockerAPICMD CmdStartAckStr_pal = new GameDockerAPICMD();
       CmdStartAckStr_pal.setCmd("bash\t-c\ttimeout 5m tail -n 5 -f /control/output.txt | { flag=0; while IFS= read -r line; do if [[ \"$line\" == *\"Disabling core dumps.\"* ]]; then echo \"startAck\"; pkill -P $$ tail; flag=1; break; fi; done; if [ $flag -eq 0 ]; then echo \"startERR\"; fi; }");
       CmdStartAckStr_pal.setCmdId("CmdStartAckStr_pal");
       CmdStartAckStr_pal.setCmdKind("start_ack");

       GameDockerAPICMD CmdStopAckStr_pal = new GameDockerAPICMD();
       CmdStopAckStr_pal.setCmd("bash\t-c\ttimeout 5m tail -n 5 -f /control/output.txt | { flag=0; while IFS= read -r line; do if [[ \"$line\" == *\"Shutdown handler: cleanup.\"* ]]; then echo \"stopAck\" ; sleep 5 ; pkill -P $$ tail ; flag=1 ; break ; fi ; done ; if [ $flag -eq 0 ]; then echo \"stopERR\" ; fi ;}");
       CmdStopAckStr_pal.setCmdId("CmdStopAckStr_pal");
       CmdStopAckStr_pal.setCmdKind("stop_ack");

       GameDockerAPICMD UserListcmd_pal = new GameDockerAPICMD();
       UserListcmd_pal.setCmd("bash\t-c\techo '] [LOG] , joined the server. ,] [LOG] , left the server.' > /control/user_cmd.txt");
       UserListcmd_pal.setCmdId("UserListcmd_pal");
       UserListcmd_pal.setCmdKind("userListCMD");

       GameDockerAPICMD running_pal = new GameDockerAPICMD();
       running_pal.setCmd("bash\t-c\ttop -b -n 1 | grep PalServ || echo 'null'");
       running_pal.setCmdId("running_pal");
       running_pal.setCmdKind("serverRun");

       GameDockerAPICMD CmdStopStr_pal = new GameDockerAPICMD();
       CmdStopStr_pal.setCmd("bash\t-c\tpkill -f PalServer-Linux");
       CmdStopStr_pal.setCmdId("CmdStopStr_pal");
       CmdStopStr_pal.setCmdKind("CmdStopStr");

       GameDockerAPICMD SearchStr_pal = new GameDockerAPICMD();
       SearchStr_pal.setCmd("PalServ");
       SearchStr_pal.setCmdId("SearchStr_pal");
       SearchStr_pal.setCmdKind("SearchStr");

       gameDockerAPICMDRepo.save(pathFolder_pal);
       gameDockerAPICMDRepo.save(pathFileList_pal);
       gameDockerAPICMDRepo.save(CmdStartAckStr_pal);
       gameDockerAPICMDRepo.save(CmdStopAckStr_pal);
       gameDockerAPICMDRepo.save(UserListcmd_pal);
       gameDockerAPICMDRepo.save(running_pal);
       gameDockerAPICMDRepo.save(CmdStopStr_pal);
       gameDockerAPICMDRepo.save(SearchStr_pal);
       gameDockerAPICMDRepo.save(makeDirStr_pal);
       gameDockerAPICMDRepo.save(delMeoStr_pal);

       GameDockerAPICMD CmdStartAckStr_terra = new GameDockerAPICMD();
       CmdStartAckStr_terra.setCmd("bash\t-c\ttimeout 5m tail -n 5 -f /control/output.txt | { flag=0; while IFS= read -r line; do if [[ \"$line\" == *\": Server started\"* ]]; then echo \"startAck\"; pkill -P $$ tail; flag=1; break; fi; done; if [ $flag -eq 0 ]; then echo \"startERR\"; fi; }");
       CmdStartAckStr_terra.setCmdId("CmdStartAckStr_terra");
       CmdStartAckStr_terra.setCmdKind("start_ack");

       GameDockerAPICMD CmdStopAckStr_terra = new GameDockerAPICMD();
       CmdStopAckStr_terra.setCmd("bash\t-c\ttimeout 5m tail -n 5 -f /control/output.txt | { flag=0; while IFS= read -r line; do if [[ \"$line\" == *\"Saving before exit...\"* ]]; then echo \"stopAck\" ; sleep 5 ; pkill -P $$ tail ; flag=1 ; break ; fi ; done ; if [ $flag -eq 0 ]; then echo \"stopERR\" ; fi ;}");
       CmdStopAckStr_terra.setCmdId("CmdStopAckStr_terra");
       CmdStopAckStr_terra.setCmdKind("stop_ack");

       GameDockerAPICMD banlist_terra = new GameDockerAPICMD();
       banlist_terra.setCmd("bash\t-c\t[ -s /config/banlist.txt ] && cat /config/banlist.txt || echo \"null\"");
       banlist_terra.setCmdId("banlist_terra");
       banlist_terra.setCmdKind("userBan");

       GameDockerAPICMD UserListcmd_terra = new GameDockerAPICMD();
       UserListcmd_terra.setCmd("bash\t-c\techo ' , has joined, , has left' > /control/user_cmd.txt");
       UserListcmd_terra.setCmdId("UserListcmd_terra");
       UserListcmd_terra.setCmdKind("userListCMD");

       GameDockerAPICMD UserList_terra = new GameDockerAPICMD();
       UserList_terra.setCmd("bash\t-c\t[ -s /control/user.txt ] && cat /control/user.txt || echo \"null\"");
       UserList_terra.setCmdId("UserList_terra");
       UserList_terra.setCmdKind("userList");

       GameDockerAPICMD pathFolder_terra = new GameDockerAPICMD();
       pathFolder_terra.setCmd("bash\t-c\techo \"/vanilla/\" > /control/dataPath.txt");
       pathFolder_terra.setCmdId("pathFolder_terra");
       pathFolder_terra.setCmdKind("pathFolder");

       GameDockerAPICMD pathFileList_terra = new GameDockerAPICMD();
       pathFileList_terra.setCmd("/config/");
       pathFileList_terra.setCmdId("pathFileList_terra");
       pathFileList_terra.setCmdKind("pathFileList");

       GameDockerAPICMD makeDirStr_terra = new GameDockerAPICMD();
       makeDirStr_terra.setCmd("mkdir\t/config/");
       makeDirStr_terra.setCmdId("makeDirStr_terra");
       makeDirStr_terra.setCmdKind("makeDirStr");

       GameDockerAPICMD delMeoStr_terra = new GameDockerAPICMD();
       delMeoStr_terra.setCmd("rm\t-rf\t/config/");
       delMeoStr_terra.setCmdId("delMeoStr_terra");
       delMeoStr_terra.setCmdKind("delMeoStr");

       GameDockerAPICMD CmdStopStr_terra = new GameDockerAPICMD();
       CmdStopStr_terra.setCmd("bash\t-c\techo 'input exit' > /control/input.txt");
       CmdStopStr_terra.setCmdId("CmdStopStr_terra");
       CmdStopStr_terra.setCmdKind("CmdStopStr");

       GameDockerAPICMD SearchStr_terra = new GameDockerAPICMD();
       SearchStr_terra.setCmd("TerrariaServer");
       SearchStr_terra.setCmdId("SearchStr_terra");
       SearchStr_terra.setCmdKind("SearchStr");

       GameDockerAPICMD running_terra = new GameDockerAPICMD();
       running_terra.setCmd("bash\t-c\tps -aux | grep TerrariaServer");
       running_terra.setCmdId("running_terra");
       running_terra.setCmdKind("serverRun");

       GameDockerAPICMD execCMD2_terra = new GameDockerAPICMD();
       execCMD2_terra.setCmd("bash\t-c\tsed -i 's/\\(CMD=\"\\.\\/TerrariaServer -x64 -config \\/config\\/serverconfig.txt -banlist \\/config\\/banlist.txt\\).*\\(\"\\)/\\1 TEXT \\2/' /vanilla/run.sh");
       execCMD2_terra.setCmdId("execCMD2_terra");
       execCMD2_terra.setCmdKind("execCMD2");


       gameDockerAPICMDRepo.save(CmdStopStr_terra);
       gameDockerAPICMDRepo.save(CmdStartAckStr_terra);
       gameDockerAPICMDRepo.save(CmdStopAckStr_terra);
       gameDockerAPICMDRepo.save(banlist_terra);
       gameDockerAPICMDRepo.save(running_terra);
       gameDockerAPICMDRepo.save(UserListcmd_terra);
       gameDockerAPICMDRepo.save(UserList_terra);
       gameDockerAPICMDRepo.save(pathFolder_terra);
       gameDockerAPICMDRepo.save(pathFileList_terra);
       gameDockerAPICMDRepo.save(SearchStr_terra);
       gameDockerAPICMDRepo.save(makeDirStr_terra);
       gameDockerAPICMDRepo.save(delMeoStr_terra);
       gameDockerAPICMDRepo.save(execCMD2_terra);

//-----------------------------------------------------------------------------------------
       mine1_16_5.addCMD(CmdMemory_Mine_1_16_5Str);
       mine1_16_5.addCMD(CmdStartAckStr_mine);
       mine1_16_5.addCMD(CmdStopAckStr_mine);
       mine1_16_5.addCMD(banlist_mine);
       mine1_16_5.addCMD(running_mine);
       mine1_16_5.addCMD(UserListcmd_mine_1_16_5);
       mine1_16_5.addCMD(UserList_mine);
       mine1_16_5.addCMD(pathFolder_mine);
       mine1_16_5.addCMD(pathFileList_mine);
       mine1_16_5.addCMD(CmdStopStr_mine);
       mine1_16_5.addCMD(SearchStr_mine);
       mine1_16_5.addCMD(makeDirStr_mine);
       mine1_16_5.addCMD(delMeoStr_mine);

       mine1_19_2.addCMD(CmdMemory_Mine_1_19_2Str);
       mine1_19_2.addCMD(CmdStartAckStr_mine);
       mine1_19_2.addCMD(CmdStopAckStr_mine);
       mine1_19_2.addCMD(banlist_mine);
       mine1_19_2.addCMD(running_mine);
       mine1_19_2.addCMD(UserListcmd_mine);
       mine1_19_2.addCMD(UserList_mine);
       mine1_19_2.addCMD(pathFolder_mine);
       mine1_19_2.addCMD(pathFileList_mine);
       mine1_19_2.addCMD(CmdStopStr_mine);
       mine1_19_2.addCMD(SearchStr_mine);
       mine1_19_2.addCMD(makeDirStr_mine);
       mine1_19_2.addCMD(delMeoStr_mine);

       mine1_20_4.addCMD(CmdMemory_Mine_1_20_4Str);
       mine1_20_4.addCMD(CmdStartAckStr_mine);
       mine1_20_4.addCMD(CmdStopAckStr_mine);
       mine1_20_4.addCMD(banlist_mine);
       mine1_20_4.addCMD(running_mine);
       mine1_20_4.addCMD(UserListcmd_mine);
       mine1_20_4.addCMD(UserList_mine);
       mine1_20_4.addCMD(pathFolder_mine);
       mine1_20_4.addCMD(pathFileList_mine);
       mine1_20_4.addCMD(CmdStopStr_mine);
       mine1_20_4.addCMD(SearchStr_mine);
       mine1_20_4.addCMD(makeDirStr_mine);
       mine1_20_4.addCMD(delMeoStr_mine);

       palworld.addCMD(CmdMemory_palworld);
       palworld.addCMD(CmdStartAckStr_pal);
       palworld.addCMD(CmdStopAckStr_pal);
       palworld.addCMD(running_pal);
       palworld.addCMD(UserListcmd_pal);
       palworld.addCMD(UserList_mine);
       palworld.addCMD(pathFolder_pal);
       palworld.addCMD(pathFileList_pal);
       palworld.addCMD(CmdStopStr_pal);
       palworld.addCMD(SearchStr_pal);
       palworld.addCMD(makeDirStr_pal);
       palworld.addCMD(delMeoStr_pal);

       terraria.addCMD(CmdMemory_terraria);
       terraria.addCMD(CmdStartAckStr_terra);
       terraria.addCMD(CmdStopAckStr_terra);
       terraria.addCMD(banlist_terra);
       terraria.addCMD(running_terra);
       terraria.addCMD(UserListcmd_terra);
       terraria.addCMD(UserList_terra);
       terraria.addCMD(pathFolder_terra);
       terraria.addCMD(pathFileList_terra);
       terraria.addCMD(CmdStopStr_terra);
       terraria.addCMD(SearchStr_terra);
       terraria.addCMD(makeDirStr_terra);
       terraria.addCMD(delMeoStr_terra);
       terraria.addCMD(execCMD2_terra);

       gameRepo.save(mine1_16_5);
       gameRepo.save(mine1_19_2);
       gameRepo.save(mine1_20_4);
       gameRepo.save(palworld);
       gameRepo.save(terraria);
   }


   private void saveDocker(
                   Game game,
                   String ServerName,
                   EdgeServer edge,
                   int port,
                   String DockerId,
                   int Ram,
                   String ServerComment,
                   Boolean Public,
                   Boolean FreeAccess,
                   User serverUser,
                   User user1, User user2, User user3,
                   User user4, User user5, User user6, User user7,
                   String tag1, String tag2, String tag3 ) {
       DockerServer docker = new DockerServer();
       docker.setGame(game);
       docker.setServerName(ServerName);
       docker.setUser(serverUser);
       docker.setBaseImage(null);
       docker.setEdgeServer(edge);
       docker.setPort(port);
       docker.setDockerId(DockerId);
       docker.setRAMCapacity(Ram);
       docker.setServerComment(ServerComment);
       docker.setPublic(Public);
       docker.setFreeAccess(FreeAccess);

       docker.addApplicant(user1);
       docker.addApplicant(user2);
       docker.addApplicant(user3);

       docker.addParticipant(user4);
       docker.addParticipant(user5);
       docker.addParticipant(user6);
       docker.addParticipant(user7);

       docker.addGameTag(gameTagRepo.findByTag(tag1));
       docker.addGameTag(gameTagRepo.findByTag(tag2));
       docker.addGameTag(gameTagRepo.findByTag(tag3));
       dockerServerRepo.save(docker);
   }


    private User saveUser(String Username, String password, String name, Boolean manager) {
        JoinDTO joinDTO = new JoinDTO();
        joinDTO.setUsername(Username);
        joinDTO.setPassword(password);
        joinService.joinProcess(joinDTO);

        User user = UserRepo.findByUsername(Username);
        user.setName(name);
        user.setEmail(Username);
        user.setPoints((long) 9999999);
        user.setAccountStatus(true);
        if (name.equals("root")) {
            user.setRole("ROLE_ADMIN");
        }
        UserRepo.save(user);
        return user;
    }


}