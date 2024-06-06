package com.neo.back.authorization.service;

import com.neo.back.authorization.dto.*;
import com.neo.back.authorization.entity.User;
import com.neo.back.authorization.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Random;


@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        System.out.println(oAuth2User);
        //도메인 id ex)google,naver
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;
        if (registrationId.equals("naver")) {

            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals("google")) {

            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        } else {

            return null;
        }

        //리소스 서버에서 발급 받은 정보로 사용자를 특정할 아이디값을 만듬

        String username = oAuth2Response.getEmail();

        User existData = userRepository.findByUsername(username);

        if (existData == null) {

            User user = new User();
            user.setUsername(oAuth2Response.getEmail());
            user.setEmail(oAuth2Response.getEmail());
            user.setPoints(1000L);
            String randomnickname = generateUniqueNickname();
            user.setName(randomnickname);
            user.setRole("ROLE_USER");

            userRepository.save(user);

            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(oAuth2Response.getEmail());
            userDTO.setName(randomnickname);
            userDTO.setRole("ROLE_USER");

            return new CustomOAuth2User(userDTO);
        } else {

            existData.setEmail(oAuth2Response.getEmail());

            String randomnickname = existData.getName();

            userRepository.save(existData);

            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(oAuth2Response.getEmail());
            userDTO.setName(randomnickname);
            userDTO.setRole(existData.getRole());

            return new CustomOAuth2User(userDTO);
        }
    }

    // 유니크 닉네임 생성 함수
    private String generateUniqueNickname() {
        String[] adjectives = {"happy", "sleepy", "grumpy", "playful", "jolly", "mischievous"};
        String[] animals = {"dolphin", "bear", "cat", "dog", "lion", "tiger"};

        Random random = new Random();
        String baseNickname = adjectives[random.nextInt(adjectives.length)] + "_" + animals[random.nextInt(animals.length)];
        String uniqueNickname = baseNickname;
        Integer counter = 1;

        uniqueNickname = baseNickname + counter;
        while (userRepository.existsByname(uniqueNickname)) {
            uniqueNickname = baseNickname + counter;
            counter++;
        }

        return uniqueNickname;
    }

}