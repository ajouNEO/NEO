package com.neo.back.infoManaging.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServerManagingGameDto {
    private Long id;
    private String port;
    private String dockerImage;
    private String name;
    private String version;
}
