package com.neo.back.docker.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.nio.file.*;
import java.util.NoSuchElementException;

import com.neo.back.springjwt.entity.User;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.neo.back.docker.dto.MyServerListDto;
import com.neo.back.docker.entity.DockerImage;
import com.neo.back.docker.exception.NotOwnerException;
import com.neo.back.docker.repository.DockerImageRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServerListService {
    private final DockerImageRepository dockerImageRepo;

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

    // public Mono<String> renameServer() {
        
    // }

}
