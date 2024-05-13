package com.neo.back.docker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateDockerDto {

    private String gameName;

    private String version;

    private Long imageNum;

    private String serverName;

    private int ramCapacity;

    private String paymentSystem;

    private int time;

    private String baseImage;

}