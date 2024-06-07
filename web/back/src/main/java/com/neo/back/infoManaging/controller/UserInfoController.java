package com.neo.back.infoManaging.controller;


import com.neo.back.authorization.entity.User;
import com.neo.back.infoManaging.dto.ManagerPostInquiryDto;
import com.neo.back.infoManaging.dto.UserInquiryListDto;
import com.neo.back.infoManaging.dto.UserPostInquiryDto;
import com.neo.back.infoManaging.service.UserInquiryService;
import com.neo.back.infoManaging.service.UserInfoService;
import com.neo.back.service.dto.UserProfileDto;
import com.neo.back.service.utility.GetCurrentUser;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class UserInfoController {

    private final GetCurrentUser getCurrentUser;
    private final RedisTemplate<String, String> template;
    private final UserInquiryService userserInquiryService;

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

    @PostMapping("/api/user/inquiry")
    public Mono<Object> postUserInquiry(@RequestBody UserPostInquiryDto inquiryData) {
        User user = getCurrentUser.getUser();
        return userserInquiryService.postUserInquiry(user,inquiryData);
    }

    @GetMapping("/api/user/inquiry")
    public Mono<Object> getUserInquiryToAnswer(@RequestParam Long inquiryId) {
        User user = getCurrentUser.getUser();
        return userserInquiryService.getUserInquiryToAnswer(user,inquiryId);
    }

    @GetMapping("/api/user/inquiry/list")
    public List<UserInquiryListDto> getUserInquiryList() {
        User user = getCurrentUser.getUser();
        return userserInquiryService.getUserInquiryList(user);
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

    @DeleteMapping("/api/user/inquiry")
    public Mono<String> deleteUserInquiry(@RequestParam Long inquiryId) {
        User user = getCurrentUser.getUser();
        return userserInquiryService.deleteUserInquiry(user,inquiryId);
    }
    
    @PostMapping("/api/manager/inquiry")
    public Mono<Object> postManagerInquiry(@RequestBody ManagerPostInquiryDto inquiryData) {
        User user = getCurrentUser.getUser();
        return userserInquiryService.postManagerInquiry(user,inquiryData);
    }

    @GetMapping("/api/manager/inquiry")
    public Mono<Object> getManagerInquiryToAnswer(@RequestParam Long inquiryId) {
        User user = getCurrentUser.getUser();
        return userserInquiryService.getManagerInquiryToAnswer(user,inquiryId);
    }

    @GetMapping("/api/manager/inquiry/list")
    public Mono<Object> getManagerInquiryList() {
        User user = getCurrentUser.getUser();
        return userserInquiryService.getManagerInquiryList(user);
    }

    @DeleteMapping("/api/manager/inquiry")
    public Mono<String> deleteManagerInquiry(@RequestParam Long inquiryId) {
        User user = getCurrentUser.getUser();
        return userserInquiryService.deleteManagerInquiry(user,inquiryId);
    }

}
