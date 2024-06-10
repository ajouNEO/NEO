package com.neo.back.mainService.dto;

import java.util.HashSet;
import java.util.Set;

import com.neo.back.mainService.entity.GameDockerAPICMD;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSettingCMDDto extends UserSettingDto  {
    private String memory;
    private Set<GameDockerAPICMD> gameDockerAPICMDs_settings = new HashSet<>();
}



