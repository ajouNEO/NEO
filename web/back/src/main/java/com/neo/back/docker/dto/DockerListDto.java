package com.neo.back.docker.dto;

import com.neo.back.springjwt.entity.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DockerListDto {
    private User userId;
    private Long Id;
}
