package com.neo.back.springjwt.service;

import com.neo.back.springjwt.dto.CustomUserDetails;
import com.neo.back.springjwt.entity.UserEntity;
import com.neo.back.springjwt.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {



        //DB에서 조회
        UserEntity userData = userRepository.findByUsername(username);

        System.out.println(userData);

        if(userData != null){
            //UserDetails에 담아서 return하면 AuthenticationManager가 검증
            System.out.println("gogo");
            System.out.println(userData);
            return new CustomUserDetails(userData);
        }

        return null;

    }
}
