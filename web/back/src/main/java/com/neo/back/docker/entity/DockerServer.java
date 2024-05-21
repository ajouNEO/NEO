package com.neo.back.docker.entity;

import com.neo.back.springjwt.entity.User;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

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

    @Lob
    private String serverComment;

    private boolean isPublic = false;

    private boolean isFreeAccess = false;

    @ManyToMany
    private Set<User> applicants = new HashSet<>();

    @ManyToMany
    private Set<User> participants = new HashSet<>();

    public void addApplicant(User applicant) {
        this.applicants.add(applicant);
    }

    public void removeApplicant(User applicant) {
        this.applicants.remove(applicant);
    }

    public void addParticipant(User participant) {
        this.participants.add(participant);
    }

    public void removeParticipant(User participant) {
        this.participants.remove(participant);
    }

    public List<String> getApplicantNames() {
        return applicants.stream()
        .map(User::getUsername)
        .collect(Collectors.toList());
    }

    public List<String> getParticipantNames() {
        return participants.stream()
        .map(User::getUsername)
        .collect(Collectors.toList());
    }
}
