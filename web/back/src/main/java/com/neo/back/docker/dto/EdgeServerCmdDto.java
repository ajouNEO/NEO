package com.neo.back.docker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EdgeServerCmdDto {
    private String cpu;
    private String memoryTotal;
    private String memoryFree;
    private String storageTotal;
    private String storageAvailable;
    private String portUse;

}