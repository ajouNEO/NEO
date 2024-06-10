package com.neo.back.mainService.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;


@Getter
@Setter
public class UserProfileDto {
    MultipartFile file;

    String profilecomment;
}
