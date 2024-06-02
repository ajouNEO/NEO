package com.neo.back.authorization.service;

public interface UserService {
    boolean changePassword(String username,String currentPassword, String newPassword);
    boolean resetPassword(String username);

    boolean checkDuplicateEmail(String email);

    boolean checkDuplicateName(String name);


}
