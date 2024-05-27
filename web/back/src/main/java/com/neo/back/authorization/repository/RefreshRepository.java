package com.neo.back.authorization.repository;

import com.neo.back.authorization.entity.RefreshEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshRepository extends JpaRepository<RefreshEntity,Integer> {

    RefreshEntity save(RefreshEntity jwt);


    Boolean existsByRefresh(String refresh);

    @Transactional
    void deleteByRefresh(String refresh);
}
