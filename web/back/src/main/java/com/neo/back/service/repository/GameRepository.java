package com.neo.back.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.neo.back.service.entity.Game;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    Game findByGameNameAndVersion(String gameName, String version);
}
