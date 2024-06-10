package com.neo.back.mainService.repository;
import com.neo.back.mainService.entity.GameServerSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameServerSettingRepository extends JpaRepository<GameServerSetting, Long> {

    Optional<GameServerSetting> findById(Long id);

}
