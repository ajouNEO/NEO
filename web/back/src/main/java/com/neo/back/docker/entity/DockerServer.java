package com.neo.back.docker.entity;

import com.neo.back.springjwt.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class DockerServer {
    
    //서버 생성시
    public DockerServer(String serverName, User user, EdgeServer edgeServer, int port, String containerId, int ramCapacity, Game game) {
        this.serverName = serverName;
        this.user = user;
        this.edgeServer = edgeServer;
        this.port = port;
        this.dockerId = containerId;
        this.RAMCapacity = ramCapacity;
        this.game = game;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serverName;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String baseImage;

    @ManyToOne
    @JoinColumn(name = "edgeServerName")
    private EdgeServer edgeServer;
    private int port;
    private String dockerId;

    private int RAMCapacity;

    @ManyToOne
    @JoinColumn(name = "game")
    private Game game;

    private String serverComment;

}
