package com.neo.back.docker.config;

import java.util.List;

import com.neo.back.docker.entity.MinecreftServerSetting;
import com.neo.back.docker.repository.GameServerSettingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.neo.back.docker.entity.EdgeServer;
import com.neo.back.docker.entity.Game;
import com.neo.back.docker.repository.EdgeServerRepository;
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


        Game game = new Game("Minecraft", "1.16.5", "mc1.16.5", minecreftServerSetting);
        gameRepo.save(game);
        game = new Game("Minecraft", "1.19.2", "mc1.19.2", minecreftServerSetting);
        gameRepo.save(game);
        game = new Game("Minecraft", "1.20.4", "mc1.20.4", minecreftServerSetting);
        gameRepo.save(game);
	}
}
