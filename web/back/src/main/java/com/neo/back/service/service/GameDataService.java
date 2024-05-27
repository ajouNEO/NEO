package com.neo.back.service.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.neo.back.authorization.entity.User;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.neo.back.service.dto.FileDataDto;
import com.neo.back.service.entity.DockerServer;
import com.neo.back.service.repository.DockerServerRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GameDataService {

    private final DockerServerRepository dockerServerRepo;
    private final WebClient.Builder webClientBuilder;
    private WebClient dockerWebClient;
    
    public ExchangeFilterFunction logRequestAndResponse() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            System.out.println("Request: " + clientRequest.method() + " " + clientRequest.url());
            clientRequest.headers().forEach((name, values) ->
                    values.forEach(value -> System.out.println(name + ": " + value)));
            return Mono.just(clientRequest);
        }).andThen(ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            System.out.println("Response: Status code " + clientResponse.statusCode());
            clientResponse.headers().asHttpHeaders().forEach((name, values) ->
                    values.forEach(value -> System.out.println(name + ": " + value)));
            return Mono.just(clientResponse);
        }));
    }

    private Mono<String> getfileAndFolderInst(String dockerId,Map<String, Serializable> getfileAndFolder) {
        return dockerWebClient.post()
        .uri("/containers/"+ dockerId +"/exec")
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(getfileAndFolder))
        .retrieve()
        .bodyToMono(String.class)
        .flatMap(fileAndFolder -> Mono.fromCallable(() -> {
            return fileAndFolder;
        }));
    }
    
    public Mono<String> getFileAndFolderListInst(String path, User user) {
        DockerServer dockerServer = dockerServerRepo.findByUser(user);
        String ip = dockerServer.getEdgeServer().getIp();
        String dockerId = dockerServer.getDockerId();
        this.dockerWebClient =  this.webClientBuilder.baseUrl("http://" + ip +":2375")
        // .filter(logRequestAndResponse())
        .build();

        String[] cmdArray = new String[]{"ls", "-l","/server/" + path};
        
        var getfileAndFolder = Map.of(
            "AttachStdin", false,
            "AttachStdout", true,
            "AttachStderr", true,
            "DetachKeys", "ctrl-p,ctrl-q",
            "Tty", false,
            "Cmd", cmdArray,
            "Env", new String[]{"FOO=bar", "BAZ=quux"}
        );
        return getfileAndFolderInst(dockerId,getfileAndFolder)
        .flatMap(response -> Mono.fromCallable(() -> {
            return response;
        }));
    }
    
    private String parseExecInstanceId(String response) {
        JSONObject jsonObject = new JSONObject(response);
        return jsonObject.getString("Id");
    }
    
    public List<FileDataDto> getFileAndFolderList(Mono<String> fileListInst, User user) {
        DockerServer dockerServer = dockerServerRepo.findByUser(user);
        String ip = dockerServer.getEdgeServer().getIp();
        String fileListInstId = parseExecInstanceId(fileListInst.block());
        this.dockerWebClient =  this.webClientBuilder.baseUrl("http://" + ip +":2375")
        // .filter(logRequestAndResponse())
        .build();
        
        var FileAndFolder = Map.of(
            "Detach", false,
            "Tty", true,
            "ConsoleSize", new int[]{80,64}
        );

        Mono<String> monoList = dockerWebClient.post()
                .uri("/exec/"+ fileListInstId +"/start")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(FileAndFolder))
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(fileAndFolder -> Mono.fromCallable(() -> {
                    return fileAndFolder;
                }));

        List<FileDataDto> List = new ArrayList<>();
        String[] lines = monoList.block().split("\\n");
        for (int i = 1; i < lines.length; i++) { 
            String line = lines[i];
            char firstChar = line.charAt(0);
            FileDataDto fileData = new FileDataDto();
            fileData.setFileName(line.split(" ")[line.split(" ").length - 1]);
            if (firstChar == 'd') {
                fileData.setIsDirectory(true);
            } else {
                fileData.setIsDirectory(false);
            }
            List.add(fileData);
        }
        return List;
    }
}
