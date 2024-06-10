package com.neo.back.otherService.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServerManagingEdgeServerDto {
    private String name;
    private int totalMem;
    private int useMem;
}
