package com.neo.back.infoManaging.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserManagingListDto {
    private String username;
    private String name;
    private Long points;
    private Long id;
    private Boolean accountStatus;
}
