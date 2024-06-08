package com.neo.back.service.service;

import java.util.Map;
import java.util.Optional;
import java.util.Collections;
import java.nio.file.*;

import com.neo.back.authorization.entity.User;
import com.neo.back.exception.DoesNotExistGameException;
import com.neo.back.exception.DoesNotExistImageException;
import com.neo.back.exception.DualServerException;
import com.neo.back.exception.LackPointException;
import com.neo.back.exception.UserCapacityExceededException;

import org.json.JSONObject;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.neo.back.service.dto.CreateDockerDto;
import com.neo.back.service.dto.EdgeServerInfoDto;
import com.neo.back.service.entity.DockerImage;
import com.neo.back.service.entity.DockerServer;
import com.neo.back.service.entity.EdgeServer;
import com.neo.back.service.entity.Game;
import com.neo.back.service.middleware.DockerAPI;
import com.neo.back.service.repository.DockerImageRepository;
import com.neo.back.service.repository.DockerServerRepository;
import com.neo.back.service.repository.EdgeServerRepository;
import com.neo.back.service.repository.GameRepository;
import com.neo.back.service.utility.MakeWebClient;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CreateDockerService {
    private final DockerAPI dockerAPI;
    private final DockerServerRepository dockerRepo;
    private final EdgeServerRepository edgeRepo;
    private final DockerImageRepository imageRepo;
    private final GameRepository gameRepo;
    private final SelectEdgeServerService selectEdgeServerService;
    private final ScheduleService scheduleService;
    private final MakeWebClient makeWebClient;
    private WebClient dockerWebClient;
    private EdgeServerInfoDto selectedEdgeServerInfo;
    private String containerId;

    public Mono<Object> createContainer(CreateDockerDto config, User user) {
        // 사용자 포인트 확인
        if (user.getPoints() < config.getRamCapacity()/2) return Mono.error(new LackPointException());


        DockerServer existingDocker = this.dockerRepo.findByUser(user);
        if (existingDocker != null) return Mono.error(new DualServerException());
        
        this.selectedEdgeServerInfo = this.selectEdgeServerService.selectingEdgeServer(config.getRamCapacity());
        if (this.selectedEdgeServerInfo == null) return Mono.error(new UserCapacityExceededException());

        this.dockerWebClient =  this.makeWebClient.makeDockerWebClient(this.selectedEdgeServerInfo.getIP());

        Game game = gameRepo.findByGameNameAndVersion(config.getGameName(), config.getVersion());
        if (game == null) return Mono.error(new DoesNotExistGameException());

        // Docker 컨테이너 생성을 위한 JSON 객체 구성
        var createContainerRequest = Map.of(
            "Image", game.getDockerImage(),
            "ExposedPorts", Map.of(
                game.getDefaultPort(), Map.of()
            ),
            "HostConfig", Map.of(
                "PortBindings", Map.of(
                    game.getDefaultPort(), Collections.singletonList(
                        Map.of("HostPort", String.valueOf(this.selectedEdgeServerInfo.getPortSelect()))
                    )
                ),
                "Memory", config.getRamCapacity() * 1024 * 1024 * 1024
            )
        );

        return this.createContainerRequest(createContainerRequest)
            .flatMap(response -> this.databaseReflection(config, game, null, user))
            .flatMap(response -> scheduleService.startScheduling(user));
        
    }

    public Mono<Object> recreateContainer(CreateDockerDto config, User user) {
        // 사용자 포인트 확인
        if (user.getPoints() < config.getRamCapacity()/2) return Mono.error(new LackPointException());
        
        DockerServer existingDocker = this.dockerRepo.findByUser(user);
        if (existingDocker != null) return Mono.error(new DualServerException());

        Optional<DockerImage> dockerImage = this.imageRepo.findById(config.getImageNum());
        
        this.selectedEdgeServerInfo = this.selectEdgeServerService.selectingEdgeServer(config.getRamCapacity());
        if (this.selectedEdgeServerInfo == null) return Mono.error(new UserCapacityExceededException());

        this.dockerWebClient =  this.makeWebClient.makeDockerWebClient(this.selectedEdgeServerInfo.getIP());

        // Docker 컨테이너 생성을 위한 JSON 객체 구성
        var createContainerRequest = Map.of(
            "Image", dockerImage.get().getImageId(),
            "ExposedPorts", Map.of(
                dockerImage.get().getGame().getDefaultPort(), Map.of()
            ),
            "HostConfig", Map.of(
                "PortBindings", Map.of(
                    dockerImage.get().getGame().getDefaultPort(), Collections.singletonList(
                        Map.of("HostPort", String.valueOf(this.selectedEdgeServerInfo.getPortSelect()))
                    )
                ),
                "Memory", config.getRamCapacity() * 1024 * 1024 * 1024
            )
        );

        config.setServerName(dockerImage.get().getServerName());

        return this.loadImage(dockerImage.get())
            .flatMap(response -> this.createContainerRequest(createContainerRequest))
            .flatMap(response -> this.databaseReflection(config, dockerImage.get().getGame(), dockerImage.get().getImageId(), user))
            .flatMap(response -> scheduleService.startScheduling(user));
    
    }

    private Mono<String> createContainerRequest(Map<String, Object> createContainerRequest) {
        return this.dockerAPI.createContainer(createContainerRequest, this.dockerWebClient)
            .flatMap(createResponse -> Mono.defer(() -> {
                String containerId = parseContainerId(createResponse);
                this.containerId = containerId;
                return this.dockerAPI.restartContainer(containerId, this.dockerWebClient);         
            }));
    }

    @Transactional
    public Mono<String> databaseReflection(CreateDockerDto config, Game game, String dockerImageId, User user) {
        
        DockerServer dockerServer = new DockerServer(
            config.getServerName(),
            user, 
            this.edgeRepo.findByIp(this.selectedEdgeServerInfo.getIP()), 
            this.selectedEdgeServerInfo.getPortSelect(), 
            this.containerId, 
            config.getRamCapacity(), 
            game);
        if (dockerImageId != null) {
            dockerServer.setBaseImage(dockerImageId);
        }

        this.dockerRepo.save(dockerServer);

        EdgeServer edgeserver = dockerServer.getEdgeServer();
        edgeserver.increaseMemoryUse(config.getRamCapacity());
        this.edgeRepo.save(edgeserver);

        return Mono.just("Container create Success");
    }

    private Mono<String> loadImage(DockerImage dockerImage) {
        Path filePath = Paths.get("/mnt/nas/dockerImage/" + dockerImage.getServerName() + "_" + dockerImage.getUser().getId() + ".tar");
        if (!Files.exists(filePath)) return Mono.error(new DoesNotExistImageException());
        
        FileSystemResource resource = new FileSystemResource(filePath);
        
        return DataBufferUtils.read(resource, new DefaultDataBufferFactory(), 4096)
            .collectList()
            .flatMap(dataBuffer -> this.dockerAPI.loadImage(dataBuffer, this.dockerWebClient));
    }

    // 컨테이너 생성 응답에서 컨테이너 ID를 파싱
    private String parseContainerId(String response) {
        JSONObject jsonObject = new JSONObject(response);
        return jsonObject.getString("Id");
    }

}

