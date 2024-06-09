package com.neo.back.authorization.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Entity
@Setter
@Getter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(unique = true) // 이메일 중복되지 않게 설정.
    private String username;

    @Column(unique = true) // 이메일 중복되지 않게 설정.
    private String email;

    @Column(unique = true) // 유저네임 중복되지 않게 설정.
    private String name;
    private String password;

    private String role;
    private Long points;

    private Boolean accountStatus = true;
    
    private String imagePath;
    private String profileComment = "안녕하세요";


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void addPoint(Long point){
        Long currentPoint = getPoints();
        currentPoint += point;
        setPoints(currentPoint);

    }
}