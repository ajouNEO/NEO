package com.neo.back.authorization.controller;

import com.neo.back.authorization.dto.*;
import com.neo.back.authorization.entity.User;
import com.neo.back.authorization.service.UserService;
import com.neo.back.authorization.util.RedisUtil;
import com.neo.back.service.repository.DockerServerRepository;
import com.neo.back.service.utility.GetCurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {


    @Autowired
    private UserService userService;

    @Autowired
    private GetCurrentUser getCurrentUser;

    @Autowired
    DockerServerRepository dockerServerRepository;

    @Autowired
    private  RedisUtil redisUtil;



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

    @PostMapping("/checkUsername")
    public Boolean checkUsernameDuplicate(@RequestBody EmailRequest request){
        String email = request.getEmail();
        boolean success = userService.checkDuplicateEmail(email);

       return success;
    }

    @PostMapping("/checkname")
    public Boolean checkUsernameDuplicate(@RequestBody NameRequest request){
        String name = request.getNickname();
        boolean success = userService.checkDuplicateName(name);

        return success;
    }

    @PostMapping("/changename")
    public ResponseEntity<String> changeNickname(@RequestBody NameRequest request){
        String name = request.getNickname();

        User user = getCurrentUser.getUser();
        ResponseEntity<String> success = userService.changenickname(user,name);

        return success;
    }



    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(){
        try{
            User user = getCurrentUser.getUser();

            if(dockerServerRepository.findByUser(user)!=null)
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("user game server should close");

            // Redis에서 해당 사용자의 데이터를 삭제
            String usernameKey = user.getUsername();
            redisUtil.deleteData(usernameKey);

            userService.deleteUser(user);
            return ResponseEntity.ok("User deleted successfully");
        }
        catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting user.");
        }


    }



}
