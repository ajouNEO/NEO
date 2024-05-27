package com.neo.back.service.repository;
import com.neo.back.service.entity.GameServerSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameServerSettingRepository extends JpaRepository<GameServerSetting, Long> {

    Optional<GameServerSetting> findById(Long id);

}
