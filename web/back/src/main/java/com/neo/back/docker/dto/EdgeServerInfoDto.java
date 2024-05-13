package com.neo.back.docker.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EdgeServerInfoDto {
    private String edgeServerID;
    private int memoryUse;
    private int memoryIdle;
    private List<String> portUses;
    private int portSelect;
    private String iP;

    private double cpuUse;
    private double cpuIdle;
    private double storageUse;
    private double storageIdle;


    public EdgeServerInfoDto(String edgeServerID){
        this.edgeServerID = edgeServerID;
    }

    public double getMemoryUsePercent() {
        return this.memoryUse * 100 /(this.memoryUse + this.memoryIdle);
    }

    public double getMemoryIdlePercent() {
        return this.memoryIdle * 100 /(this.memoryUse + this.memoryIdle);
    }
    
    public double getStorageUsePercent() {
        return this.storageUse * 100 /(this.storageUse + this.storageIdle);
    }

    public double getStorageIdlePercent() {
        return this.storageIdle * 100 /(this.storageUse + this.storageIdle);
    }
}
