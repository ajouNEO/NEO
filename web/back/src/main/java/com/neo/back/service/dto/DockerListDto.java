package com.neo.back.service.dto;

import com.neo.back.authorization.entity.User;

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
