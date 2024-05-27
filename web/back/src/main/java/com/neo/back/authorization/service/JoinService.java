package com.neo.back.authorization.service;

import com.neo.back.authorization.dto.JoinDTO;
import com.neo.back.authorization.entity.User;
import com.neo.back.authorization.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class JoinService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public JoinService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder){

        this.userRepository = userRepository;
       this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public void joinProcess(JoinDTO joinDTO){

        String username = joinDTO.getUsername();
        String password = joinDTO.getPassword();



        Boolean isExist = userRepository.existsByUsername(username);

        if (isExist) {

            return;
        }

        User User = new User();

        User.setUsername(username);
        User.setPassword(bCryptPasswordEncoder.encode(password));
        User.setRole("ROLE_ADMIN");

        System.out.println(username);

        System.out.println(User);

        userRepository.save(User);

    }

}
