package com.neo.back.otherService.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServerManagingDockerServerDto {
    private Long id;
    private Boolean isFreeAccess;
    private Boolean isPublic;
    private int maxPlayer;
    private int port;
    private int ram;
    private int userNumber;
    private Instant date_created;
    private Long gameId;
    private Long userId;
    private String baseImage;
    private String dockerId;
    private String edgeServerName;
    private String serverName;
    private String serverComment;
}
