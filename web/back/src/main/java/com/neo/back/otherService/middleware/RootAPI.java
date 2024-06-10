package com.neo.back.otherService.middleware;

import com.neo.back.authorization.entity.User;
import com.neo.back.otherService.entity.UserInquiry;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RootAPI {
    
    public Boolean checkUser(User user,UserInquiry inquiry){
        if(user.getId() == inquiry.getUser().getId())return true;
        else return false;
    }
}
