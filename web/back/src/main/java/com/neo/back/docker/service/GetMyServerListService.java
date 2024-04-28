package com.neo.back.docker.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.neo.back.docker.dto.MyServerListDto;
import com.neo.back.docker.entity.DockerImage;
import com.neo.back.docker.repository.DockerImageRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class GetMyServerListService {
    private final DockerImageRepository dockerImageRepo;
    
    public GetMyServerListService(DockerImageRepository dockerImageRepo) {
        this.dockerImageRepo = dockerImageRepo;
    }

    public List<MyServerListDto> getMyServerList() {
        List<DockerImage> dockerImages = dockerImageRepo.findByUser(null);

        return dockerImages.stream()
                            .map(image -> new MyServerListDto(image.getId(), image.getGame().getGame(), image.getServerName(), image.getDate()))
                            .collect(Collectors.toList());
    }
}