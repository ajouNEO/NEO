package com.neo.back.docker.entity;

import com.neo.back.springjwt.entity.User;

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
public class DockerImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serverName;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    private String imageId;

    private Long size;

    private String date;

    @ManyToOne
    @JoinColumn(name = "game")
    private Game game;

}