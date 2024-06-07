package com.neo.back.infoManaging.controller;


import com.neo.back.authorization.dto.NameRequest;
import com.neo.back.authorization.entity.User;
import com.neo.back.authorization.repository.UserRepository;
import com.neo.back.infoManaging.dto.ManagerPostInquiryDto;
import com.neo.back.infoManaging.dto.UserInquiryListDto;
import com.neo.back.infoManaging.dto.UserInquiryToAnswer;
import com.neo.back.infoManaging.dto.UserPostInquiryDto;
import com.neo.back.infoManaging.service.UserInquiryService;
import com.neo.back.service.utility.GetCurrentUser;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
@RestController
@RequiredArgsConstructor
public class UserInfoController {

    private final GetCurrentUser getCurrentUser;
    private final RedisTemplate<String, String> template;
    private final UserInquiryService userserInquiryService;

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
