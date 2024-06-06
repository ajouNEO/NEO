package com.neo.back.authorization.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String username;

    private String imagePath;

    @OneToOne
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

}
