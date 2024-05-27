package com.neo.back.service.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.nio.file.*;
import java.util.NoSuchElementException;

import com.neo.back.service.dto.UserSettingDto;
import com.neo.back.service.middleware.DockerAPI;
import com.neo.back.authorization.entity.User;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.neo.back.service.dto.MyServerListDto;
import com.neo.back.service.entity.DockerImage;
import com.neo.back.service.exception.NotOwnerException;
import com.neo.back.service.repository.DockerImageRepository;

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

            if (dockerImage.get().getUser() != user) {
                throw new NotOwnerException();
            } 

            Path path = dockerImagePath.resolve(dockerImage.get().getServerName() + "_" + dockerImage.get().getUser().getId() + ".tar");

            Files.delete(path);
            dockerImageRepo.deleteById(imageNum);
            return Mono.just("Delete image success");
        } catch (NoSuchElementException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("This container does not exist in database"));
        } catch (NoSuchFileException e) {
            dockerImageRepo.deleteById(imageNum);
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("This container does not exist in storage"));
        } catch (NotOwnerException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body("This container is not owned by this user"));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    public List<UserSettingDto>  getUserContainerId(User user) {
        UserSettingDto UserSetting = dockerAPI.settingIDS(user);
        List<UserSettingDto> userSettingsList = Collections.singletonList(UserSetting);
        return userSettingsList;
    }

    // public Mono<String> renameServer() {
        
    // }

}
