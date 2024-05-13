package com.neo.back.docker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServerInfoDto {
    String serverName;
    String ip;
    int port;
    String gameName;
    String version;
    int RAMCapacity;
    boolean isPublic;
    String serverComment;
}
