package com.neo.back.docker.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class Game {
    public Game(String gameName, String version, String dockerImage, GameServerSetting serverSetting) {
        this.gameName = gameName;
        this.version = version;
        this.dockerImage = dockerImage;
        this.defaultSetting = serverSetting;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String gameName;
    private String version;

    private String dockerImage;

    @ManyToOne
    @JoinColumn
    private GameServerSetting defaultSetting;
}
