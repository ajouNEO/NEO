package com.neo.back.infoManaging.controller;


import com.neo.back.authorization.entity.User;
import com.neo.back.infoManaging.dto.ManagerPostInquiryDto;
import com.neo.back.infoManaging.dto.UserInquiryListDto;
import com.neo.back.infoManaging.dto.UserManagingPointDto;
import com.neo.back.infoManaging.dto.UserPostInquiryDto;
import com.neo.back.infoManaging.service.UserInquiryService;
import com.neo.back.infoManaging.service.UserManagingService;
import com.neo.back.infoManaging.service.UserInfoService;
import com.neo.back.service.dto.UserProfileDto;
import com.neo.back.service.utility.GetCurrentUser;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequiredArgsConstructor
public class UserManagingController {

    private final GetCurrentUser getCurrentUser;
    private final RedisTemplate<String, String> template;
    private final UserInquiryService userserInquiryService;
    private final UserInfoService userInfoService;
    private final UserManagingService userManagingService;

    
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

    @DeleteMapping("/api/user/inquiry")
    public Mono<Object> deleteUserInquiry(@RequestParam Long inquiryId) {
        User user = getCurrentUser.getUser();
        return this.userserInquiryService.deleteUserInquiry(user,inquiryId);
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
    public ResponseEntity<String> setProfileImage(@RequestParam("file")MultipartFile file) throws IOException {
        User user = getCurrentUser.getUser();
        return userInfoService.saveProfileImage(user, file);
    }

    @PostMapping("api/user/profilecomment")
    public boolean setProfileComment(@RequestBody UserProfileDto userProfileDto){

        User user = getCurrentUser.getUser();

        System.out.println(userProfileDto.getProfilecomment());
        Boolean success = userInfoService.saveProfileComment(user,userProfileDto);

        return success;
    }

    @GetMapping("api/user/profilecomment")
    public String getProfileComment(){

        User user = getCurrentUser.getUser();

        return userInfoService.LoadProfileComment(user);
    }
    
    @GetMapping("api/user/profileimage/other")
    public ResponseEntity<Object> getProfileImage_other(@RequestParam String userName) throws IOException {
        User user = getCurrentUser.getUser();
        if(!userInfoService.isUserHaveApplicant(user, userName)) return ResponseEntity.ok("not applicant");
        return userInfoService.getProfileImage_other(user,userName);
    }

    @GetMapping("api/user/profilecomment/other")
    public String getProfileComment_other(@RequestParam String userName){
        User user = getCurrentUser.getUser();
        if(!userInfoService.isUserHaveApplicant(user, userName)) return "not applicant";
        return userInfoService.LoadProfileComment_other(user,userName);
    }

    @PostMapping("/api/admin/user/inquiry")
    public ResponseEntity<Object> postManagerInquiry(@RequestBody ManagerPostInquiryDto inquiryData) {
        User user = getCurrentUser.getUser();
        return this.userserInquiryService.postManagerInquiry(user,inquiryData);
    }

    @GetMapping("/api/admin/user/inquiry")
    public ResponseEntity<Object> getManagerInquiryToAnswer(@RequestParam Long inquiryId) {
        User user = getCurrentUser.getUser();
        return this.userserInquiryService.getManagerInquiryToAnswer(user,inquiryId);
    }

    @GetMapping("/api/admin/user/inquiry/list")
    public ResponseEntity<Object> getManagerInquiryList() {
        User user = getCurrentUser.getUser();
        return this.userserInquiryService.getManagerInquiryList(user);
    }

    @DeleteMapping("/api/admin/user/inquiry")
    public ResponseEntity<Object> deleteManagerInquiry(@RequestParam Long inquiryId) {
        User user = getCurrentUser.getUser();
        return this.userserInquiryService.deleteManagerInquiry(user,inquiryId);
    }

    @GetMapping("/api/admin/user/list")
    public ResponseEntity<Object> getUserLsitByManager() {
        User user = getCurrentUser.getUser();
        return this.userManagingService.getUserLsitByManager(user);
    }

    @PutMapping("/api/admin/user/point/add")
    public ResponseEntity<Object> addPointToUser(@RequestBody UserManagingPointDto userData) {
        User user = getCurrentUser.getUser();
        return this.userManagingService.addPointToUser(user,userData);
    }

    @PutMapping("/api/admin/user/point/sub")
    public ResponseEntity<Object> subPointToUser(@RequestBody UserManagingPointDto userData) {
        User user = getCurrentUser.getUser();
        return this.userManagingService.subPointToUser(user,userData);
    }

    @PutMapping("/api/admin/user/account/stop")
    public ResponseEntity<Object> stopUserAccount(@RequestParam Long userId) {
        User user = getCurrentUser.getUser();
        return this.userManagingService.stopUserAccount(user,userId);
    }

    @PutMapping("/api/admin/user/account/active")
    public ResponseEntity<Object> activeUserAccount(@RequestParam Long userId) {
        User user = getCurrentUser.getUser();
        return this.userManagingService.activeUserAccount(user,userId);
    }
}
