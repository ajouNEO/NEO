package com.neo.back.infoManaging.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.neo.back.authorization.entity.User;
import com.neo.back.infoManaging.service.UserInfoService;
import com.neo.back.service.utility.GetCurrentUser;

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
