package com.neo.back.infoManaging.middleware;

import com.neo.back.authorization.entity.User;
import com.neo.back.infoManaging.entity.UserInquiry;

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
