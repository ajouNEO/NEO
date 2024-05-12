package com.neo.back.docker.service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.neo.back.springjwt.entity.User;
import org.json.JSONObject;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.neo.back.docker.entity.DockerImage;
import com.neo.back.docker.entity.DockerServer;
import com.neo.back.docker.entity.EdgeServer;
import com.neo.back.docker.exception.DoNotHaveServerException;
import com.neo.back.docker.exception.NasServerException;
import com.neo.back.docker.middleware.DockerAPI;
import com.neo.back.docker.repository.DockerImageRepository;
import com.neo.back.docker.repository.DockerServerRepository;
import com.neo.back.docker.repository.EdgeServerRepository;
import com.neo.back.docker.utility.MakeWebClient;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@Transactional
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
        try {
            DockerServer dockerServer = dockerServerRepo.findByUser(user);
            if (dockerServer == null) throw new DoNotHaveServerException();

            this.dockerWebClient =  this.makeWebClient.makeDockerWebClient(dockerServer.getEdgeServer().getIp());
            
            return this.stopContainerRequest(dockerServer)
                .flatMap(result -> this.makeImageRequest(dockerServer))
                .flatMap(result -> this.deleteContainerRequest(dockerServer))
                .flatMap(result -> this.saveDockerImage(dockerServer))
                .flatMap(result -> this.databaseReflection(dockerServer))
                .flatMap(result -> this.deleteLeftDockerImage())
                .flatMap(result -> Mono.just("Server close & save success"));

        } catch (DoNotHaveServerException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("This user does not have an open server."));
        } catch (WebClientResponseException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("dockerAPI error"));
        }
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

    private Mono<String> saveDockerImage(DockerServer dockerServer) {
        Path dockerImagePath = Paths.get("/mnt/nas/dockerImage");
        if (!Files.exists(dockerImagePath)) {
            try {
                Files.createDirectories(dockerImagePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Path path = dockerImagePath.resolve(dockerServer.getServerName() + "_" + dockerServer.getUser().getId() + ".tar");
        return this.dockerWebClient.get()
            .uri("/images/{imageName}/get", this.imageId)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .exchangeToMono(response -> {
                try {FileChannel channel = FileChannel.open(path,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE);
                    return response.bodyToFlux(DataBuffer.class)
                            .flatMap(dataBuffer -> saveToNas(dataBuffer, channel))
                            .then(Mono.just("file save success"))
                            .onErrorResume(e -> Mono.just("Failed to upload file: " + e.getMessage()))
                            .doFinally(type -> {
                                try {
                                    if (channel != null) channel.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                } catch (IOException e) {
                    return Mono.error(e);
                }
            })
            .flatMap(result -> Mono.just("Save image success"));
    }

    private Mono<String> databaseReflection(DockerServer dockerServer) {
        return this.dockerAPI.getImageInfo(this.imageId, this.dockerWebClient)
            .flatMap(response -> {
                DockerImage dockerImage;
                if (dockerServer.getBaseImage() != null) {
                    dockerImage = dockerImageRepo.findByImageId(dockerServer.getBaseImage());
                } else {
                    dockerImage = new DockerImage();
                } 
                
                dockerImage.setServerName(dockerServer.getServerName());
                dockerImage.setUser(dockerServer.getUser());
                dockerImage.setImageId(this.imageId);
                dockerImage.setSize(parseImageSize(response));
                dockerImage.setDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                dockerImage.setGame(dockerServer.getGame());
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

    @SuppressWarnings("deprecation")
    private Mono<String> saveToNas(DataBuffer dataBuffer, FileChannel channel){
        try {
            ByteBuffer byteBuffer = dataBuffer.asByteBuffer();
            while (byteBuffer.hasRemaining()) {
                System.out.println("Writing data...");
                channel.write(byteBuffer);
            }
            DataBufferUtils.release(dataBuffer);
            return Mono.empty();
        } catch (Exception e) {
            throw new NasServerException();
        }
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
