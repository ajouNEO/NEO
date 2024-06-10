package com.neo.back.authorization.entity;


import com.neo.back.otherService.entity.UserInquiry;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
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
    private String profileComment;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserInquiry> userInquiries;


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

    public Long addPoint(Long point){
        Long currentPoint = getPoints();
        currentPoint += point;
        setPoints(currentPoint);
        return currentPoint;
    }
}