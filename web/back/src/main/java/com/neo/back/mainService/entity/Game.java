package com.neo.back.mainService.entity;

import java.util.HashSet;
import java.util.Set;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class Game {
    public Game(String gameName, String version, String dockerImage, String defaultPort, String settingFilePath, String settingFileName, String itemSeparator, String keyValueSeparator, String maxPlayerKey) {
        this.gameName = gameName;
        this.version = version;
        this.dockerImage = dockerImage;
        this.defaultPort = defaultPort;
        this.settingFilePath = settingFilePath;
        this.settingFileName = settingFileName;
        this.itemSeparator = itemSeparator;
        this.keyValueSeparator = keyValueSeparator;
        this.maxPlayerKey = maxPlayerKey;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String gameName;
    private String version;

    private String defaultPort;

    private String dockerImage;

    private String settingFilePath;

    private String settingFileName;

    private String itemSeparator;

    private String keyValueSeparator;

    private String maxPlayerKey;

    @ManyToMany
    private Set<GameDockerAPICMD> gameDockerAPICMDs = new HashSet<>();

    
    public void addCMD(GameDockerAPICMD cmd) {
        this.gameDockerAPICMDs.add(cmd);
    }

    public void removeCMD(GameDockerAPICMD cmd) {
        this.gameDockerAPICMDs.remove(cmd);
    }

}