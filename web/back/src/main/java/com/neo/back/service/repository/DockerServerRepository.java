package com.neo.back.service.repository;

import com.neo.back.service.entity.DockerServer;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.neo.back.authorization.entity.User;

import io.lettuce.core.dynamic.annotation.Param;

import java.util.List;



@Repository
public interface DockerServerRepository extends JpaRepository<DockerServer, Long> {
   
    Optional<DockerServer> findById(Long id);
    DockerServer findByUser(User user);
    List<DockerServer> findByIsPublic(boolean isPublic);

    @Query("SELECT ds FROM DockerServer ds JOIN ds.applicants u WHERE u = :user")
    List<DockerServer> findAllByApplicantUserId(@Param("user") User user);

    @Query("SELECT ds FROM DockerServer ds JOIN ds.participants p WHERE p = :user")
    List<DockerServer> findAllByParticipantUserId(@Param("user") User user);
}
