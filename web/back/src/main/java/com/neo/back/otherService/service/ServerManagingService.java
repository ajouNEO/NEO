package com.neo.back.otherService.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.neo.back.authorization.entity.User;
import com.neo.back.authorization.repository.UserRepository;
import com.neo.back.otherService.dto.ServerManagingDockerImageDto;
import com.neo.back.otherService.dto.ServerManagingDockerServerDto;
import com.neo.back.otherService.dto.ServerManagingEdgeServerDto;
import com.neo.back.otherService.dto.ServerManagingGameDto;
import com.neo.back.mainService.entity.DockerImage;
import com.neo.back.mainService.entity.DockerServer;
import com.neo.back.mainService.entity.EdgeServer;
import com.neo.back.mainService.entity.Game;
import com.neo.back.mainService.repository.DockerImageRepository;
import com.neo.back.mainService.repository.DockerServerRepository;
import com.neo.back.mainService.repository.EdgeServerRepository;
import com.neo.back.mainService.repository.GameRepository;

import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class ServerManagingService {

    private final GameRepository gameRepo;
    private final UserRepository userRepo;
    private final DockerImageRepository dockerImageRepo;
    private final EdgeServerRepository edgeServerRepo;
    private final DockerServerRepository dockerServerRepo;

    public ResponseEntity<Object> getGame(User user){
        List<Game> Games = this.gameRepo.findAll();
        return ResponseEntity.ok(Games.stream()
        .map(game -> new ServerManagingGameDto(
            game.getId(),
            game.getDefaultPort(),
            game.getDockerImage(),
            game.getGameName(),
            game.getVersion()))
        .collect(Collectors.toList()));
    }

    public ResponseEntity<Object> setGame(User user,ServerManagingGameDto GameData){
        Optional<Game> games = this.gameRepo.findById(GameData.getId());

        if(games.isPresent()){
            Game game = games.get();
            game.setDefaultPort(GameData.getPort());
            game.setGameName(GameData.getName());
            game.setVersion(GameData.getVersion());
            game.setDockerImage(GameData.getDockerImage());
            this.gameRepo.save(game);
            return ResponseEntity.ok("success to set game");
        }
        else{
            return ResponseEntity.ok("fail to set game");
        }
    }

    public ResponseEntity<Object> getDockerImage(User user){
        List<DockerImage> DockerImages = this.dockerImageRepo.findAll();
        return ResponseEntity.ok(DockerImages.stream()
        .map(dockerImage -> new ServerManagingDockerImageDto(
            dockerImage.getId(),
            dockerImage.getSize(),
            dockerImage.getGame().getId(),
            dockerImage.getUser().getId(),
            dockerImage.getServerName(),
            dockerImage.getImageId(),
            dockerImage.getDate()))
        .collect(Collectors.toList()));
    }

    public ResponseEntity<Object> setDockerImage(User user,ServerManagingDockerImageDto DockerImageData){
        Optional<DockerImage> DockerImages = this.dockerImageRepo.findById(DockerImageData.getId());

        if(DockerImages.isPresent()){
            DockerImage DockerImage = DockerImages.get();
            DockerImage.setSize(DockerImageData.getSize());
            DockerImage.setGame(this.gameRepo.findById(DockerImageData.getGameId()).get());
            DockerImage.setUser(this.userRepo.findById(DockerImageData.getUserId()).get());
            DockerImage.setServerName(DockerImageData.getServerName());
            DockerImage.setImageId(DockerImageData.getImageId());
            DockerImage.setDate(DockerImageData.getDate());
            this.dockerImageRepo.save(DockerImage);
            return ResponseEntity.ok("success to set dockerImage");
        }
        else{
            return ResponseEntity.ok("fail to set dockerImage");
        }
    }

    public ResponseEntity<Object> getEdgeServer(User user){
        List<EdgeServer> edgeServers = this.edgeServerRepo.findAll();
        return ResponseEntity.ok(edgeServers.stream()
        .map(edgeserver -> new ServerManagingEdgeServerDto(
            edgeserver.getEdgeServerName(),
            edgeserver.getMemoryTotal(),
            edgeserver.getMemoryUse()))
        .collect(Collectors.toList()));
    }

    public ResponseEntity<Object> setEdgeServer(User user,ServerManagingEdgeServerDto EdgeserverData){
        Optional<EdgeServer> edgeServers = this.edgeServerRepo.findById(EdgeserverData.getName());

        if(edgeServers.isPresent()){
            EdgeServer edgeServer = edgeServers.get();
            edgeServer.setMemoryTotal(EdgeserverData.getTotalMem());
            edgeServer.setMemoryUse(EdgeserverData.getUseMem());
            this.edgeServerRepo.save(edgeServer);
            return ResponseEntity.ok("success to set edgeServer");
        }
        else{
            return ResponseEntity.ok("fail to set edgeServer");
        }
    }

    public ResponseEntity<Object> getDockerServer(User user){
        List<DockerServer> dockerServers = this.dockerServerRepo.findAll();
        return ResponseEntity.ok(dockerServers.stream()
        .map(dockerServer -> new ServerManagingDockerServerDto(
            dockerServer.getId(),
            dockerServer.isFreeAccess(),
            dockerServer.isPublic(),
            dockerServer.getMaxPlayer(),
            dockerServer.getPort(),
            dockerServer.getRAMCapacity(),
            dockerServer.getUserNumber(),
            dockerServer.getCreatedDate(),
            dockerServer.getGame().getId(),
            dockerServer.getUser().getId(),
            dockerServer.getBaseImage(),
            dockerServer.getDockerId(),
            dockerServer.getEdgeServer().getEdgeServerName(),
            dockerServer.getServerName(),
            dockerServer.getServerComment()))
        .collect(Collectors.toList()));
    }

    public ResponseEntity<Object> setDockerServer(User user,ServerManagingDockerServerDto dockerServerData){
        Optional<DockerServer> dockerServers = this.dockerServerRepo.findById(dockerServerData.getId());

        if(dockerServers.isPresent()){
            DockerServer dockerserver = dockerServers.get();
            dockerserver.setFreeAccess(dockerServerData.getIsFreeAccess());
            dockerserver.setPublic(dockerServerData.getIsPublic());
            dockerserver.setMaxPlayer(dockerServerData.getMaxPlayer());
            dockerserver.setPort(dockerServerData.getPort());
            dockerserver.setRAMCapacity(dockerServerData.getRam());
            dockerserver.setUserNumber(dockerServerData.getUserNumber());
            // dockerserver.set
            dockerserver.setGame(this.gameRepo.findById(dockerServerData.getGameId()).get());
            dockerserver.setUser(this.userRepo.findById(dockerServerData.getId()).get());
            dockerserver.setEdgeServer(this.edgeServerRepo.findByEdgeServerName(dockerServerData.getEdgeServerName()));
            dockerserver.setServerName(dockerServerData.getServerName());
            dockerserver.setServerComment(dockerServerData.getServerComment());
            this.dockerServerRepo.save(dockerserver);
            return ResponseEntity.ok("success to set dockerserver");
        }
        else{
            return ResponseEntity.ok("fail to set dockerserver");
        }
    }
    
}
