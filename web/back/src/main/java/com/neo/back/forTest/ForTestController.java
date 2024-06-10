package com.neo.back.forTest;

import com.neo.back.mainService.entity.DockerImage;
import com.neo.back.mainService.entity.DockerServer;
import com.neo.back.mainService.entity.EdgeServer;
import com.neo.back.mainService.repository.DockerImageRepository;
import com.neo.back.mainService.repository.DockerServerRepository;
import com.neo.back.mainService.repository.EdgeServerRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ForTestController {

    private final DockerImageRepository dockerImageRepo;
    private final DockerServerRepository dockerServerRepo;
    private final EdgeServerRepository edgeServerRepo;

    @GetMapping("/test/dockerImage")
    public ResponseEntity<List<testDockerImageDto>> getTable1Data() {
        // 예시 데이터 반환
        List<DockerImage> dataList = dockerImageRepo.findAll(); // 데이터베이스에서 모든 데이터를 조회합니다.
        List<testDockerImageDto> dataDTOList = dataList.stream()
                .map(data -> new testDockerImageDto(data.getId(), data.getServerName(), data.getUser().getName(), data.getImageId(), data.getSize(), data.getDate(), data.getGame().getGameName() + data.getGame().getVersion()))
                .collect(Collectors.toList()); // Entity 리스트를 DTO 리스트로 변환합니다.

        return ResponseEntity.ok(dataDTOList);
    }

    @GetMapping("/test/dockerServer")
    public ResponseEntity<List<testDockerServerDto>> getTable2Data() {
        // 예시 데이터 반환
        List<DockerServer> dataList = dockerServerRepo.findAll(); // 데이터베이스에서 모든 데이터를 조회합니다.
        List<testDockerServerDto> dataDTOList = dataList.stream()
                .map(data -> new testDockerServerDto(data.getId(), data.getServerName(), data.getUser().getName(), data.getEdgeServer().getEdgeServerName(), data.getPort(), data.getDockerId(), data.getRAMCapacity(), data.getGame().getGameName() + data.getGame().getVersion(), data.getBaseImage()))
                .collect(Collectors.toList()); // Entity 리스트를 DTO 리스트로 변환합니다.

        return ResponseEntity.ok(dataDTOList);
    }

    @GetMapping("/test/edgeServer")
    public ResponseEntity<List<testEdgeServerDto>> getTable3Data() {
        // 예시 데이터 반환
        List<EdgeServer> dataList = edgeServerRepo.findAll(); // 데이터베이스에서 모든 데이터를 조회합니다.
        List<testEdgeServerDto> dataDTOList = dataList.stream()
                .map(data -> new testEdgeServerDto(data.getEdgeServerName(), data.getMemoryTotal(), data.getMemoryUse()))
                .collect(Collectors.toList()); // Entity 리스트를 DTO 리스트로 변환합니다.
        return ResponseEntity.ok(dataDTOList);
    }

}