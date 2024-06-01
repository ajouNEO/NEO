package com.neo.back.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserBanMineDto {
    String uuid;
    String name;
    String created;
    String source;
    String expires;
    String reason;
}
