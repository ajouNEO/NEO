package com.neo.back.infoManaging.middleware;

import com.neo.back.authorization.entity.User;
import com.neo.back.authorization.repository.UserRepository;
import com.neo.back.infoManaging.entity.UserInquiry;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RootAPI {

    private final UserRepository userRepo;
    
    public Boolean checkManager(User user){
        if(user.getManager())return true;
        else return false;
    }

    public Boolean checkUser(User user,UserInquiry inquiry){
        if(user.getId() == inquiry.getUser().getId())return true;
        else return false;
    }
}
