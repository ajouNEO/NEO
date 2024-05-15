package com.neo.back.docker.repository;

import com.neo.back.docker.entity.DockerServer;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.neo.back.springjwt.entity.User;
import java.util.List;



@Repository
public interface DockerServerRepository extends JpaRepository<DockerServer, Long> {
   
    Optional<DockerServer> findById(Long id);
    DockerServer findByUser(User user);
    List<DockerServer> findByIsPublic(boolean isPublic);
}
