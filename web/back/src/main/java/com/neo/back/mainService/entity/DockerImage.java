package com.neo.back.mainService.entity;

import com.neo.back.authorization.entity.User;

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

    public void setDockerImage (String serverName, User user, String imageId, Long size, String date, Game game) {
        this.serverName = serverName;
        this.user = user;
        this.imageId = imageId;
        this.size = size;
        this.date = date;
        this.game = game;
    }
}