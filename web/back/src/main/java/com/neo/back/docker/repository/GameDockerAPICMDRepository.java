package com.neo.back.docker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.neo.back.docker.entity.GameDockerAPICMD;

@Repository
public interface GameDockerAPICMDRepository extends JpaRepository<GameDockerAPICMD, Long> {
    GameDockerAPICMD findBycmdId(String cmdId);
}