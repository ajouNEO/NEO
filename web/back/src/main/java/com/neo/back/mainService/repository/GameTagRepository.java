package com.neo.back.mainService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.neo.back.mainService.entity.GameTag;

@Repository
public interface GameTagRepository extends JpaRepository<GameTag, Long> {
    GameTag findByTag(String Tag);
}
