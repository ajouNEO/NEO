package com.neo.back.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserServerListDto {
    String name;
    String usernameORother;
    String Source;
    String time;
    String expires;
    String reason;
}
