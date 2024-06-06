package com.neo.back.service.service;

import java.util.Map;
import java.util.Optional;
import java.util.Collections;
import java.nio.file.*;
import java.time.Instant;

import com.neo.back.authorization.entity.User;
import org.json.JSONObject;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.neo.back.service.dto.CreateDockerDto;
import com.neo.back.service.dto.EdgeServerInfoDto;
import com.neo.back.service.entity.DockerImage;
import com.neo.back.service.entity.DockerServer;
import com.neo.back.service.entity.EdgeServer;
import com.neo.back.service.entity.Game;
import com.neo.back.service.exception.UserCapacityExceededException;
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
        if (user.getPoints() < 1) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User Point isn't enough for create"));
        }
        try {

            DockerServer existingDocker = this.dockerRepo.findByUser(user);
            if (existingDocker != null) throw new IllegalStateException();
            
            this.selectedEdgeServerInfo = this.selectEdgeServerService.selectingEdgeServer(config.getRamCapacity());
            if (this.selectedEdgeServerInfo == null) throw new UserCapacityExceededException();

            this.dockerWebClient =  this.makeWebClient.makeDockerWebClient(this.selectedEdgeServerInfo.getIP());

            Game game = gameRepo.findByGameNameAndVersion(config.getGameName(), config.getVersion());
            if (game == null) throw new IllegalArgumentException();

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
                .flatMap(response -> this.pointScheduling(user));
        
        } catch (IllegalStateException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body("This user already has an open server"));
        } catch (UserCapacityExceededException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("No servers are available"));
        } catch (IllegalArgumentException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("It's a game that doesn't exist"));
        } catch (WebClientResponseException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("dockerAPI error"));
        } catch (Exception e) {
            return Mono.just(e);
        }
    }

    public Mono<Object> recreateContainer(CreateDockerDto config, User user) {
        // 사용자 포인트 확인
        if (user.getPoints() < 1) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User Point isn't enough for create"));
        }
        try {
            DockerServer existingDocker = this.dockerRepo.findByUser(user);
            if (existingDocker != null) throw new IllegalStateException();

            Optional<DockerImage> dockerImage = this.imageRepo.findById(config.getImageNum());
            
            this.selectedEdgeServerInfo = this.selectEdgeServerService.selectingEdgeServer(config.getRamCapacity());
            if (this.selectedEdgeServerInfo == null) throw new UserCapacityExceededException();

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
                .flatMap(response -> this.pointScheduling(user));
        
        } catch (IllegalStateException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body("This user already has an open server"));
        } catch (UserCapacityExceededException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("No servers are available"));
        } catch (IllegalArgumentException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("It's a game that doesn't exist"));
        } catch (WebClientResponseException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("dockerAPI error"));
        } catch (NoSuchFileException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("This image does not exist in storage"));
        } catch (Exception e) {
            return Mono.just(e);
        }
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

    private Mono<String> loadImage(DockerImage dockerImage) throws NoSuchFileException {
        Path filePath = Paths.get("/mnt/nas/dockerImage/" + dockerImage.getServerName() + "_" + dockerImage.getUser().getId() + ".tar");
        if (!Files.exists(filePath)) throw new NoSuchFileException(filePath.toString());
        
        FileSystemResource resource = new FileSystemResource(filePath);
        
        return DataBufferUtils.read(resource, new DefaultDataBufferFactory(), 4096)
            .collectList()
            .flatMap(dataBuffer -> this.dockerAPI.loadImage(dataBuffer, this.dockerWebClient));
    }

    private Mono<Object> pointScheduling(User user) {
        Instant startTime = Instant.now();

        DockerServer dockerServer = dockerRepo.findByUser(user);
        String dockerId = dockerServer.getDockerId();

        scheduleService.scheduleServiceEndWithPoints(user, dockerId, startTime, user.getPoints(), dockerServer.getRAMCapacity());
        scheduleService.startTrackingUser(user,dockerId);
        return Mono.just(ResponseEntity.ok("Container created successfully"));
    }

    // 컨테이너 생성 응답에서 컨테이너 ID를 파싱
    private String parseContainerId(String response) {
        JSONObject jsonObject = new JSONObject(response);
        return jsonObject.getString("Id");
    }

}

