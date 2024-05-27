package com.neo.back.authorization.dto;

import lombok.Getter;

@Getter
public class PasswordChangeRequestDTO {

    private String currentPassword;
    private String newPassword;

    public PasswordChangeRequestDTO(String currentPassword, String newPassword){
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

}
