package com.neo.back.service.service;

import com.neo.back.service.entity.DockerServer;
import com.neo.back.service.entity.Game;
import com.neo.back.service.exception.DoNotHaveServerException;
import com.neo.back.service.middleware.DockerAPI;
import com.neo.back.service.repository.DockerServerRepository;
import com.neo.back.service.utility.MakeWebClient;

import com.neo.back.authorization.entity.User;
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

import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

@Service
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
            String filePathInContainer = dockerServer.getGame().getSettingFilePath() + dockerServer.getGame().getSettingFileName();
            Path localPath = Path.of("/mnt/nas/serverSetting/" + user.getName() + ".tar");

            // Docker 컨테이너로부터 파일 받아오기
            return this.getDockerContainerFile(containerId, filePathInContainer, localPath)
                    .flatMap(response -> this.settingFormatConversion(dockerServer.getGame().getGameName(), localPath));
            
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

            Game game = dockerServer.getGame();

            this.dockerWebClient =  this.makeWebClient.makeDockerWebClient(dockerServer.getEdgeServer().getIp());
            String dockerId = dockerServer.getDockerId();

            StringBuilder result = new StringBuilder();
            for (Map.Entry<String, String> entry : setting.entrySet()) {
                if (result.length() > 0) {
                    result.append(game.getItemSeparator());
                }
                result.append(entry.getKey()).append(game.getKeyValueSeparator()).append(entry.getValue());
            }

            if ("Palworld".equals(game.getGameName())) {
                result.append(")");
                result.insert(0, "OptionSettings=(");
                result.insert(0, "[/Script/Pal.PalGameWorldSettings]\n");
            }

            System.out.println(result.toString());
            // content 문자열을 바이트 배열로 변환
            byte[] contentBytes = result.toString().getBytes(StandardCharsets.UTF_8);

            // 파일 내용을 tar 파일로 압축
            byte[] tarFile = this.createTarContent(dockerServer.getGame().getSettingFileName(), contentBytes);

            // tar 파일을 저장할 경로
            Path tarPath = Path.of("/mnt/nas/serverSetting/" + user.getName() + ".tar");

            // tar 파일 바이트 배열을 실제 파일로 저장
            Files.write(tarPath, tarFile);

            // Docker API를 통해 파일을 컨테이너에 복사
            return this.changeFileinContainer(dockerServer.getGame().getSettingFilePath(), dockerId, tarFile);

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

    private Mono<Object> settingFormatConversion(String gameName, Path localPath) {
        try {
            String propertiesString  = this.extractPropertiesFromTar(localPath.toString());

            JSONObject json = new JSONObject();

            if ("Minecraft".equals(gameName)) {
                String[] lines = propertiesString .split("\n");

                for (String line : lines) {
                    if (!line.startsWith("#") && !line.trim().isEmpty()) {
                        String[] keyValue = line.split("=", 2);
                        if (keyValue.length == 2) {
                            json.put(keyValue[0], keyValue[1]);
                        }
                    }
                }
            } else if ("Palworld".equals(gameName)) {
                propertiesString = propertiesString.replaceAll("\n", "");
                propertiesString = propertiesString.replaceAll("\\[\\/Script\\/Pal.PalGameWorldSettings\\]", "");
                propertiesString = propertiesString.replaceAll("OptionSettings=\\(", "");
                propertiesString = propertiesString.replaceAll("\\)", "");
                String[] lines = propertiesString .split(",");

                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        String[] keyValue = line.split("=", 2);
                        if (keyValue.length == 2) {
                            json.put(keyValue[0], keyValue[1]);
                        }
                    }
                }
            }
            return Mono.just(json.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error extracting server.properties"));
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

    private byte[] createTarContent(String fileName, byte[] fileContent) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             TarArchiveOutputStream tarOut = new TarArchiveOutputStream(out)) {

            // TarArchiveEntry 설정 (파일 이름은 임의로 지정)
            TarArchiveEntry entry = new TarArchiveEntry(fileName);
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

    private Mono<Object> changeFileinContainer(String filePath, String containerId, byte[] tarFile) {
        return  this.dockerAPI.uploadFile(containerId, filePath, tarFile, this.dockerWebClient)
                .thenReturn("success file updated");
    }

}