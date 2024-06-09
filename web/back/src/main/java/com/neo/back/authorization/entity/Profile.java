package com.neo.back.authorization.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Profile {
    public Profile(String username) {
        this.username = username;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String username;

    private String imagePath;

    private String profilecomment = "안녕하세요";

    @OneToOne
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

}
