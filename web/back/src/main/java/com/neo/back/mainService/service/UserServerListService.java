package com.neo.back.mainService.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.nio.file.*;
import java.util.NoSuchElementException;

import com.neo.back.mainService.dto.UserSettingDto;
import com.neo.back.mainService.middleware.DockerAPI;
import com.neo.back.authorization.entity.User;
import com.neo.back.exception.NotOwnerException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.neo.back.mainService.dto.MyServerListDto;
import com.neo.back.mainService.entity.DockerImage;
import com.neo.back.mainService.repository.DockerImageRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserServerListService {
    private final DockerImageRepository dockerImageRepo;

    private final DockerAPI dockerAPI;

    public List<MyServerListDto> getServerList(User user) {
        List<DockerImage> dockerImages = dockerImageRepo.findByUser(user);

        return dockerImages.stream()
            .map(image -> new MyServerListDto(image.getId(), image.getGame().getGameName(),image.getGame().getVersion(), image.getServerName(), image.getDate()))
            .collect(Collectors.toList());
    }

    public Mono<Object> deleteServer(Long imageNum, User user) {
        try {
            Path dockerImagePath = Paths.get("/mnt/nas/dockerImage");
            Optional<DockerImage> dockerImage = dockerImageRepo.findById(imageNum);

            if (dockerImage.get().getUser() != user) return Mono.error(new NotOwnerException());

            Path path = dockerImagePath.resolve(dockerImage.get().getServerName() + "_" + dockerImage.get().getUser().getId() + ".tar");

            Files.delete(path);
            dockerImageRepo.deleteById(imageNum);
            return Mono.just("Delete image success");
        } catch (NoSuchElementException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("This container does not exist in database"));
        } catch (NoSuchFileException e) {
            dockerImageRepo.deleteById(imageNum);
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("This container does not exist in storage"));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    public List<UserSettingDto>  getUserContainerId(User user) {
        UserSettingDto UserSetting = dockerAPI.settingIDS(user);
        List<UserSettingDto> userSettingsList = Collections.singletonList(UserSetting);
        return userSettingsList;
    }

}
