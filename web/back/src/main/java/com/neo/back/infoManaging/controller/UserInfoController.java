package com.neo.back.infoManaging.controller;


import com.neo.back.authorization.entity.User;
import com.neo.back.authorization.repository.UserRepository;
import com.neo.back.service.utility.GetCurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserInfoController {

    private final GetCurrentUser getCurrentUser;
    private final RedisTemplate<String, String> template;


    @GetMapping("/api/point")
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

    @GetMapping("/api/nickname")
    public String getNickname(){
        User user = getCurrentUser.getUser();
        return user.getName();
    }


}
