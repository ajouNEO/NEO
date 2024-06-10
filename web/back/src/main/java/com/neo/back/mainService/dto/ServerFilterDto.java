package com.neo.back.mainService.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServerFilterDto {
    String game_name;
    String version;
    Boolean is_free_access;
    List<String> tags;
}
