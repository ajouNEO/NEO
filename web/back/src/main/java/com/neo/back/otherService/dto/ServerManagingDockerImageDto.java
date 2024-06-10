package com.neo.back.otherService.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServerManagingDockerImageDto {
    private Long id;
    private Long size;
    private Long gameId;
    private Long userId;
    private String serverName;
    private String imageId;
    private String date;
}
