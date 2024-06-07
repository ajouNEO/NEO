package com.neo.back.infoManaging.service;

import com.neo.back.authorization.entity.Profile;
import com.neo.back.authorization.entity.User;
import com.neo.back.authorization.repository.ProfileRepository;
import com.neo.back.authorization.repository.UserRepository;
import com.neo.back.service.dto.UserProfileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class UserInfoService {

    private static final String NAS_BASE_PATH = "/mnt/nas/profileImage";


    private final UserRepository userRepository;

    private final ProfileRepository profileRepository;


    public void saveProfileImage(User user, MultipartFile file) throws IOException {
        Profile profile = user.getProfile();
        String fileName = user.getName() + ".tar";
        Path filePath = Paths.get(NAS_BASE_PATH + fileName);
        Files.write(filePath, file.getBytes());
        profile.setImagePath(filePath.toString());

        userRepository.save(user); // CascadeType.ALL로 인해 profile도 함께 저장됨

        return;

    }

    public void saveProfileComment(User user, UserProfileDto userProfileDto){
        Profile profile = user.getProfile();
        profile.setProfilecomment(userProfileDto.getProfilecomment());

        userRepository.save(user);

        return;
    }

    public byte[] LoadProfileImage(User user) throws IOException {

        Profile profile = user.getProfile();

        Path filePath = Paths.get(profile.getImagePath());

        return Files.readAllBytes(filePath);
    }

    public String LoadProfileComment(User user){
        Profile profile = user.getProfile();

        String profilecomment = profile.getProfilecomment();

        return profilecomment;

    }


}
