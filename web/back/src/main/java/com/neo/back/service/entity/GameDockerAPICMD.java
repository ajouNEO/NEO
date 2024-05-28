package com.neo.back.service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class GameDockerAPICMD {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gameDockerAPIId;

    @Column(length = 100, unique = true)
    private String cmdId;

    @Column(length = 700)
    private String cmd;

    @Column(length = 700)
    private String gameTag;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

}
