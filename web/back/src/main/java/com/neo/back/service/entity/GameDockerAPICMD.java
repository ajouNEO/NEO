package com.neo.back.service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class GameDockerAPICMD {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gameDockerAPIId;

    @Column(length = 100)
    private String cmdKind;

    @Column(length = 100, unique = true)
    private String cmdId;

    @Column(length = 700)
    private String cmd;


}
