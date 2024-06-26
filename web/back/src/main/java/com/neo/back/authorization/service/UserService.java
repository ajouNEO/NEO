package com.neo.back.authorization.service;

import com.neo.back.authorization.entity.User;
import org.springframework.http.ResponseEntity;

public interface UserService {
    boolean changePassword(String username,String currentPassword, String newPassword);
    boolean resetPassword(String username);

    boolean checkDuplicateEmail(String email);

    boolean checkDuplicateName(String name);

    ResponseEntity<String> changenickname(User user , String name);

    void deleteUser(User user);


}
