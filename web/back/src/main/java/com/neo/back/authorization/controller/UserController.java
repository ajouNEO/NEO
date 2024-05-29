package com.neo.back.authorization.controller;

import com.neo.back.authorization.dto.PasswordChangeRequestDTO;
import com.neo.back.authorization.dto.resetRequest;
import com.neo.back.authorization.service.UserService;
import com.neo.back.authorization.dto.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {


    @Autowired
    private UserService userService;

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @RequestBody PasswordChangeRequestDTO request) {
        String username = userDetails.getUsername();
        boolean success = userService.changePassword(username, request.getCurrentPassword(), request.getNewPassword());

        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().body("Current password is incorrect.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody resetRequest request) {
        String username = request.getUsername();
        System.out.println(username);
        boolean success = userService.resetPassword(username);

        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().body("Bad Request");
        }
    }

}
