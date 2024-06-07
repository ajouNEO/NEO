package com.neo.back.infoManaging.controller;


import com.jcraft.jsch.UserInfo;
import com.neo.back.authorization.dto.NameRequest;
import com.neo.back.authorization.entity.User;
import com.neo.back.authorization.repository.UserRepository;
import com.neo.back.infoManaging.service.UserInfoService;
import com.neo.back.service.dto.UserProfileDto;
import com.neo.back.service.utility.GetCurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class UserInfoController {

    private final GetCurrentUser getCurrentUser;
    private final RedisTemplate<String, String> template;

    private final UserInfoService userInfoService;

    @GetMapping("/api/user/point")
    public Long getPoint(){
        User user = getCurrentUser.getUser();

        ValueOperations<String, String> valueOperations = template.opsForValue();
        String points = valueOperations.get(user.getUsername());

        if (points != null) {
            // Redis에 데이터가 있으면 반환

            return Long.parseLong(points);
        }
        else{
            return user.getPoints();
        }
    }

    @GetMapping("/api/user/nickname")
    public String getNickname(){
        User user = getCurrentUser.getUser();
        return user.getName();
    }


    @GetMapping("api/user/profileimage")
    public byte[] getProfileImage() throws IOException {
        User user = getCurrentUser.getUser();

        return userInfoService.LoadProfileImage(user);

    }

    @PostMapping("api/user/profileimage")
    public void setProfileImage(@RequestParam("file")MultipartFile file) throws IOException {
        User user = getCurrentUser.getUser();

        userInfoService.saveProfileImage(user, file);

        return;
    }

    @PostMapping("api/user/profilecomment")
    public void setProfileComment(@RequestBody UserProfileDto userProfileDto){

        User user = getCurrentUser.getUser();

        System.out.println(userProfileDto.getProfilecomment());
        userInfoService.saveProfileComment(user,userProfileDto);

        return;
    }

    @GetMapping("api/user/profilecomment")
    public String getProfileComment(){

        User user = getCurrentUser.getUser();

        return userInfoService.LoadProfileComment(user);
    }


}
