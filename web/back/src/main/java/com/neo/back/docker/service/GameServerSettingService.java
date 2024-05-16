package com.neo.back.docker.service;

import com.neo.back.docker.entity.DockerServer;
import com.neo.back.docker.exception.DoNotHaveServerException;
import com.neo.back.docker.middleware.DockerAPI;
import com.neo.back.docker.repository.DockerServerRepository;
import com.neo.back.docker.utility.MakeWebClient;

import com.neo.back.springjwt.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class GameServerSettingService {

    private final DockerServerRepository dockerServerRepo;
    private final MakeWebClient makeWebClient;
    private final DockerAPI dockerAPI;
    private WebClient dockerWebClient;

    public Mono<Object> getServerSetting(User user) {
        try {
            DockerServer dockerServer = dockerServerRepo.findByUser(user);
            if (dockerServer == null) throw new DoNotHaveServerException();

            this.dockerWebClient =  this.makeWebClient.makeDockerWebClient(dockerServer.getEdgeServer().getIp());
            String containerId = dockerServer.getDockerId();
            String filePathInContainer = dockerServer.getGame().getDefaultSetting().getSettingFilePath();
            Path localPath = Path.of("/mnt/nas/serverSetting/" + user.getUsername() + ".tar");

            // Docker 컨테이너로부터 파일 받아오기
            return this.getDockerContainerFile(containerId, filePathInContainer, localPath)
                    .flatMap(response -> Mono.just(this.settingFormatConversion(localPath)));
            
        } catch (DoNotHaveServerException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("This user does not have an open server"));
        } catch (WebClientResponseException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("dockerAPI error"));
        } catch (Exception e) {
            return Mono.just(e);
        }

    }

    public Mono<Object> setServerSetting(Map<String, String> setting, User user) {
        try {
            DockerServer dockerServer = dockerServerRepo.findByUser(user);
            if (dockerServer == null) throw new DoNotHaveServerException();

            this.dockerWebClient =  this.makeWebClient.makeDockerWebClient(dockerServer.getEdgeServer().getIp());
            String dockerId = dockerServer.getDockerId();

            StringBuilder result = new StringBuilder();
            for (Map.Entry<String, String> entry : setting.entrySet()) {
                if (result.length() > 0) {
                    result.append("\n");
                }
                result.append(entry.getKey()).append("=").append(entry.getValue());
            }

            System.out.println(result.toString());
            // content 문자열을 바이트 배열로 변환
            byte[] contentBytes = result.toString().getBytes(StandardCharsets.UTF_8);

            // 파일 내용을 tar 파일로 압축
            byte[] tarFile = this.createTarContent(contentBytes);

            // tar 파일을 저장할 경로
            Path tarPath = Path.of("/mnt/nas/serverSetting/" + user.getUsername() + ".tar");

            // tar 파일 바이트 배열을 실제 파일로 저장
            Files.write(tarPath, tarFile);

            // Docker API를 통해 파일을 컨테이너에 복사
            return this.changeFileinContainer(dockerId, tarFile);

        } catch (DoNotHaveServerException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("This user does not have an open server"));
        } catch (WebClientResponseException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("dockerAPI error"));
        } catch (Exception e) {
            return Mono.just(e);
        }
    }



    @SuppressWarnings("deprecation")
    private Mono<String> getDockerContainerFile(String containerId, String filePathInContainer, Path localPath) {
        return this.dockerAPI.downloadFile(containerId, filePathInContainer, this.dockerWebClient)
            .flatMap(dataBuffer -> {
                // 받아온 tar 파일을 로컬에 저장
                try (WritableByteChannel channel = Files.newByteChannel(localPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
                    // DataBuffer에서 데이터를 읽어 로컬 파일에 쓰기
                    channel.write(dataBuffer.asByteBuffer());
                    return Mono.just("File received and saved as " + localPath.toString());
                } catch (IOException e) {
                    return Mono.error(e);
                }
            });
    }

    private ResponseEntity<String> settingFormatConversion(Path localPath) {
        try {
            String propertiesString  = this.extractPropertiesFromTar(localPath.toString());

            JSONObject json = new JSONObject();
            String[] lines = propertiesString .split("\n");

            for (String line : lines) {
                if (!line.startsWith("#") && !line.trim().isEmpty()) {
                    String[] keyValue = line.split("=", 2);
                    if (keyValue.length == 2) {
                        json.put(keyValue[0], keyValue[1]);
                    }
                }
            }
            return ResponseEntity.ok(json.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error extracting server.properties");
        }
    }

    private String extractPropertiesFromTar(String localPath) throws IOException {
        try (TarArchiveInputStream tarInput = new TarArchiveInputStream(new FileInputStream(localPath))) {
            tarInput.getNextTarEntry(); // server.properties 파일로 바로 이동
            ByteArrayOutputStream contentBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = tarInput.read(buffer)) > 0) {
                contentBuffer.write(buffer, 0, len);
            }
            return contentBuffer.toString(StandardCharsets.UTF_8.name());
        }
    }

    private byte[] createTarContent(byte[] fileContent) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             TarArchiveOutputStream tarOut = new TarArchiveOutputStream(out)) {

            // TarArchiveEntry 설정 (파일 이름은 임의로 지정)
            TarArchiveEntry entry = new TarArchiveEntry("server.properties");
            entry.setSize(fileContent.length); // 파일 크기 설정
            tarOut.putArchiveEntry(entry);

            // 파일 내용 쓰기
            tarOut.write(fileContent);
            tarOut.closeArchiveEntry();

            // tarOut을 닫아야 tar 파일 완성
            tarOut.finish();

            // 완성된 tar 파일의 바이트 배열 반환
            return out.toByteArray();
        }
    }

    private Mono<Object> changeFileinContainer(String containerId, byte[] tarFile) {
        return  this.dockerAPI.uploadFile(containerId, "/server", tarFile, this.dockerWebClient)
                .thenReturn("success file updated");
    }














    public Mono<String> executeCommand(String containerId, String command) {
        DockerServer dockerServer = dockerServerRepo.findByUser(null);
        if (dockerServer == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "This user does not have an open server."));
        }
        this.dockerWebClient = this.makeWebClient.makeDockerWebClient(dockerServer.getEdgeServer().getIp());
        
        var createExecRequest = Map.of(
            "AttachStdout", true,
            "AttachStderr", true,
            "Cmd", List.of("/bin/sh", "-c", command)
        );

        var startExecRequest = Map.of("Detach", false, "Tty", false);

        return this.dockerAPI.makeExec(containerId, createExecRequest, this.dockerWebClient)
                .flatMap(execCreationResponse -> {
                    String execId = (String) execCreationResponse.get("Id");

                    // Exec 시작 및 결과 수집
                    return this.dockerAPI.startExec(execId, startExecRequest, this.dockerWebClient);
                });
    }

}