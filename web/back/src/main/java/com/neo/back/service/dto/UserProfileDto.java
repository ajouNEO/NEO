package com.neo.back.service.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;


@Getter
@Setter
public class UserProfileDto {
    MultipartFile file;

    String profilecomment;
}
