package com.neo.back.authorization.service;

import com.neo.back.authorization.dto.JoinDTO;
import com.neo.back.authorization.entity.User;
import com.neo.back.authorization.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class JoinService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public JoinService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder){

        this.userRepository = userRepository;
       this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public ResponseEntity<?> joinProcess(JoinDTO joinDTO){

        //이메일 형식 검증
        if(!isEmailValid(joinDTO.getUsername())){
            return new ResponseEntity<>("이메일 형식이 올바르지 않습니다.", HttpStatus.BAD_REQUEST);

        }

        String username = joinDTO.getUsername();
        String name = joinDTO.getName();
        String password = joinDTO.getPassword();
        Boolean isEmailExist = userRepository.existsByUsername(username);


        Boolean isUsernameExist = userRepository.existsByname(name);

        if(isEmailExist){
            System.out.println("이메일이 이미 존재합니다.");
            return new ResponseEntity<>("이메일이 이미 존재합니다.", HttpStatus.CONFLICT);
        }

        if (isUsernameExist) {
            System.out.println("사용자 이름이 이미 존재합니다.");
            return new ResponseEntity<>("사용자 이름이 이미 존재합니다.", HttpStatus.CONFLICT);
        }

        User User = new User();

        User.setPassword(bCryptPasswordEncoder.encode(password));
        User.setRole("ROLE_USER");
        User.setName(name);
        User.setUsername(username);
        User.setEmail(username);
        User.setPoints((long) 1000);
        User.setAccountStatus(false);

        userRepository.save(User);

        return new ResponseEntity<>("회원 가입이 성공적으로 완료되었습니다.", HttpStatus.CREATED);

    }

    private boolean isEmailValid(String email) {
        Pattern pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

}
