package com.neo.back.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.neo.back.service.entity.DockerImage;
import java.util.List;
import com.neo.back.authorization.entity.User;


@Repository
public interface DockerImageRepository extends JpaRepository<DockerImage, Long> {
    List<DockerImage> findByUser(User user);
    DockerImage findByImageId(String imageId);

    void deleteByUserId(Long userid);
}
