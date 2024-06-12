package com.neo.back.mainService.service;

import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.neo.back.authorization.entity.User;
import com.neo.back.exception.DoNotHaveServerException;
import com.neo.back.exception.NasServerException;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.neo.back.mainService.entity.DockerImage;
import com.neo.back.mainService.entity.DockerServer;
import com.neo.back.mainService.entity.EdgeServer;
import com.neo.back.mainService.middleware.DockerAPI;
import com.neo.back.mainService.repository.DockerImageRepository;
import com.neo.back.mainService.repository.DockerServerRepository;
import com.neo.back.mainService.repository.EdgeServerRepository;
import com.neo.back.utility.MakeWebClient;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CloseDockerService {
    private final DockerAPI dockerAPI;
    private final DockerServerRepository dockerServerRepo;
    private final DockerImageRepository dockerImageRepo;
    private final EdgeServerRepository edgeServerRepo;
    private final MakeWebClient makeWebClient;
    private WebClient dockerWebClient;
    private String imageId;

    public Mono<Object> closeDockerService(User user) {

        DockerServer dockerServer = dockerServerRepo.findByUser(user);
        if (dockerServer == null) return Mono.error(new DoNotHaveServerException());

        this.dockerWebClient =  this.makeWebClient.makeDockerWebClient(dockerServer.getEdgeServer().getIp());
        
        return this.stopContainerRequest(dockerServer)
            .flatMap(result -> this.makeImageRequest(dockerServer))
            .flatMap(result -> this.deleteContainerRequest(dockerServer))
            .flatMap(result -> {
                try {
                    return this.saveDockerImage(dockerServer);
                } catch (NasServerException e) {
                    e.printStackTrace();
                    return Mono.error(new NasServerException());
                }
            })
            .onErrorResume(NasServerException.class, e -> Mono.error(new NasServerException()))
            .flatMap(result -> this.databaseReflection(dockerServer))
            .flatMap(result -> this.deleteLeftDockerImage())
            .flatMap(result -> Mono.just("Server close & save success"));


    }



    private Mono<String> stopContainerRequest(DockerServer dockerServer) {
        return this.dockerAPI.stopContainer(dockerServer.getDockerId(), this.dockerWebClient);
    }

    private Mono<String> makeImageRequest(DockerServer dockerServer) {
        return this.dockerAPI.commitContainer(dockerServer.getDockerId(), this.dockerWebClient)
            .flatMap(commitResponse -> {
                String imageId = parseImageId(commitResponse);
                this.imageId = imageId;
                return Mono.just("Make image success");
            });
    }

    private Mono<String> deleteContainerRequest(DockerServer dockerServer) {
        return this.dockerAPI.deleteContainer(dockerServer.getDockerId(), this.dockerWebClient);
    }

    private Mono<Object> saveDockerImage(DockerServer dockerServer) throws NasServerException {
        try {
            Path dockerImagePath = Paths.get("/mnt/nas/dockerImage");
            if (!Files.exists(dockerImagePath)) Files.createDirectories(dockerImagePath);

            Path path = dockerImagePath.resolve(dockerServer.getServerName() + "_" + dockerServer.getUser().getId() + ".tar");

            FileChannel channel = FileChannel.open(path,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE);

            return this.dockerAPI.getImage(this.imageId, this.dockerWebClient, channel);
        
        } catch (Exception e) {
            e.printStackTrace();
            throw new NasServerException();
        }
    }

    @Transactional
    public Mono<String> databaseReflection(DockerServer dockerServer) {
        return this.dockerAPI.getImageInfo(this.imageId, this.dockerWebClient)
            .flatMap(response -> {
                DockerImage dockerImage;
                if (dockerServer.getBaseImage() != null) {
                    dockerImage = dockerImageRepo.findByImageId(dockerServer.getBaseImage());
                } else {
                    dockerImage = new DockerImage();
                } 
                
                dockerImage.setDockerImage(
                    dockerServer.getServerName(),
                    dockerServer.getUser(),
                    this.imageId,
                    parseImageSize(response),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    dockerServer.getGame()
                );
                this.dockerImageRepo.save(dockerImage);

                this.dockerServerRepo.deleteById(dockerServer.getId());

                EdgeServer edgeServer = dockerServer.getEdgeServer();
                edgeServer.setMemoryUse(edgeServer.getMemoryUse() - dockerServer.getRAMCapacity());
                this.edgeServerRepo.save(edgeServer);

                return Mono.just("Database Reflection success");
            });
    }

    private Mono<String> deleteLeftDockerImage() {
        return this.dockerAPI.deleteImage(this.imageId, this.dockerWebClient);
    }

    private String parseImageId(String response) {
        JSONObject jsonObject = new JSONObject(response);
        return jsonObject.getString("Id");
    }

    private Long parseImageSize(String imageInfo) {
        JSONObject jsonObject = new JSONObject(imageInfo);
        return jsonObject.getLong("Size");
    }

}
