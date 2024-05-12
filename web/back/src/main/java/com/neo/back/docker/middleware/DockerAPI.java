package com.neo.back.docker.middleware;

import java.util.Map;
import java.util.List;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DockerAPI {

    public Mono<String> createContainer(Map<String, Object> requestMap, WebClient dockerWebClient) {
        return dockerWebClient.post()
            .uri("/containers/create")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(requestMap))
            .retrieve()
            .bodyToMono(String.class);
    }

    public Mono<String> startContainer(String containerId, WebClient dockerWebClient) {
        return dockerWebClient.post()
            .uri("/containers/" + containerId + "/start")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToMono(String.class)
            .thenReturn("Container started with ID: " + containerId);
    }

    public Mono<String> restartContainer(String containerId, WebClient dockerWebClient) {
        return dockerWebClient.post()
            .uri("/containers/" + containerId + "/restart")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToMono(String.class)
            .thenReturn("Container started with ID: " + containerId);
    }

    public Mono<String> stopContainer(String containerId, WebClient dockerWebClient) {
        return dockerWebClient.post()
            .uri("/containers/" + containerId + "/stop")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToMono(String.class)
            .then(Mono.just("Stop container success"));
    }

    public Mono<String> deleteContainer(String containerId, WebClient dockerWebClient) {
        return dockerWebClient.delete()
            .uri("/containers/" + containerId)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToMono(String.class)
            .then(Mono.just("Delete container success"));
    }

    public Mono<String> commitContainer(String containerId, WebClient dockerWebClient) {
        return dockerWebClient.post()
            .uri(uriBuilder -> uriBuilder.path("/commit")
                .queryParam("container", containerId)
                //.queryParam("repo", dockerServer.getServerName()) // 한글로하면 오류남
                //.queryParam("author", author)
                .build())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToMono(String.class);
    }

    public Mono<String> getContainerInfo(String containerId, WebClient dockerWebClient) {
        return dockerWebClient
                .get()
                .uri("/containers/{containerId}/stats?stream=false", containerId)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> getImageInfo(String imageId, WebClient dockerWebClient) {
        return dockerWebClient.get()
            .uri("/images/{imageName}/json", imageId)
            .retrieve()
            .bodyToMono(String.class);
    }

    // public Mono<String> getImage(String imageId, WebClient dockerWebClient) {
    //     return dockerWebClient.get()
    //         .uri("/images/{imageName}/get", imageId)
    //         .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_OCTET_STREAM_VALUE);
    // }

    public Mono<String> loadImage(List<DataBuffer> dataBuffer, WebClient dockerWebClient) {
        return dockerWebClient.post()
            .uri("/images/load") // Docker 이미지 로드 API
            .contentType(MediaType.valueOf("application/x-tar"))
            .body(BodyInserters.fromDataBuffers(Flux.fromIterable(dataBuffer)))
            .retrieve()
            .bodyToMono(String.class)
            .then(Mono.just("Load image success"));
    }

    public Mono<String> deleteImage(String imageId, WebClient dockerWebClient) {
        return dockerWebClient.delete()
            .uri("/images/{imageName}", imageId)
            .retrieve()
            .bodyToMono(String.class);
    }

    public Mono<String> getContainerList(WebClient dockerWebClient) {
        return dockerWebClient.get()
            .uri("/containers/json")
            .retrieve()
            .bodyToMono(String.class);
    }

    public Mono<String> getImageList(String imageId, WebClient dockerWebClient) {
        return dockerWebClient.get()
            .uri("/images/json")
            .retrieve()
            .bodyToMono(String.class);
    }

    @SuppressWarnings("rawtypes")
    public Mono<Map> makeExec(String containerId, Map<String, Object> requestMap, WebClient dockerWebClient) {
        return dockerWebClient.post()
            .uri("/containers/{id}/exec", containerId)
            .bodyValue(requestMap)
            .retrieve()
            .bodyToMono(Map.class);
    }

    public Mono<String> startExec(String execId, Map<String, Boolean> startExecRequest, WebClient dockerWebClient) {
        return dockerWebClient.post()
            .uri("/exec/{id}/start", execId)
            .bodyValue(startExecRequest)
            .retrieve()
            .bodyToMono(String.class);
    }

    public Mono<DataBuffer> downloadFile(String containerId, String path, WebClient dockerWebClient) {
        return dockerWebClient.get()
                .uri("/containers/" + containerId + "/archive?path=" + path)
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .retrieve()
                .bodyToMono(DataBuffer.class);
    }

    public Mono<String> uploadFile(String containerId, String path, byte[] tarFile, WebClient dockerWebClient) {
        return dockerWebClient.put()
                .uri("/containers/" + containerId + "/archive?path=" + path)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .bodyValue(tarFile)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Map<String,Object> makeExecInst(String[] Mes){
        return Map.of(
            "AttachStdin", false,
            "AttachStdout", true,
            "AttachStderr", true,
            "DetachKeys", "ctrl-p,ctrl-q",
            "Tty", false,
            "Cmd", Mes,
            "Env", new String[]{"FOO=bar", "BAZ=quux"}
        );
    }

    public Map<String,Boolean> makeExecStartInst(){
        return  Map.of(
            "Detach", false,
            "Tty", true
        );
    }

    public String[] split_tap(String cmd){
        return  cmd.split("\t");
    }

}
