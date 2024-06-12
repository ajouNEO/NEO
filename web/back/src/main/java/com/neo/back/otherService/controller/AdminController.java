package com.neo.back.otherService.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.neo.back.authorization.entity.User;
import com.neo.back.otherService.service.UserInfoService;
import com.neo.back.utility.GetCurrentUser;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final UserInfoService userInfoService;
    private final GetCurrentUser getCurrentUser;

    @GetMapping("/api/admin")
    public String adminp() {


        return "admin Controller";
    }

    @GetMapping("/api/user/admin")
    public ResponseEntity<String> isAdmin() {
        User user = getCurrentUser.getUser();
        return userInfoService.isAdmin(user);
    }

}
