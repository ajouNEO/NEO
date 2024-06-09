package com.neo.back.infoManaging.service;

import com.neo.back.authorization.entity.Profile;
import com.neo.back.authorization.entity.User;
import com.neo.back.authorization.repository.ProfileRepository;
import com.neo.back.authorization.repository.UserRepository;
import com.neo.back.service.dto.UserProfileDto;
import com.neo.back.service.entity.DockerServer;
import com.neo.back.service.repository.DockerServerRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserInfoService {

    private static final String NAS_BASE_PATH = "/mnt/nas/profileImage/";


    private final UserRepository userRepo;
    private final DockerServerRepository dockerServerRepo;
    private final ProfileRepository profileRepo;


    public ResponseEntity<String> saveProfileImage(User user, MultipartFile file) throws IOException {
        Profile profile = user.getProfile();
        String fileName = user.getName() + ".jpg";
        Path filePath = Paths.get(NAS_BASE_PATH + fileName);
        Files.write(filePath, file.getBytes());
        profile.setImagePath(filePath.toString());

        userRepo.save(user); // CascadeType.ALL로 인해 profile도 함께 저장됨

        return ResponseEntity.ok("success");

    }

    public boolean saveProfileComment(User user, UserProfileDto userProfileDto){
        Profile profile = user.getProfile();
        profile.setProfilecomment(userProfileDto.getProfilecomment());

        userRepo.save(user);

        return true;
    }

    public byte[] LoadProfileImage(User user) throws IOException {

        Profile profile = user.getProfile();
        if(profile.getImagePath() == null){
            Random random = new Random();
            int randomNumber = 1 + random.nextInt(6);
            profile.setImagePath(NAS_BASE_PATH + "sample_"+ randomNumber +".jpg");
            profileRepo.save(profile);
        }
        Path filePath = Paths.get(profile.getImagePath());

        return Files.readAllBytes(filePath);
    }

    public String LoadProfileComment(User user){
        Profile profile = user.getProfile();

        String profilecomment = profile.getProfilecomment();

        return profilecomment;

    }

    public String LoadProfileEmail(User user) {
        String Email = user.getEmail();

        return Email;
    }

    public ResponseEntity<Object> getProfileImage_other(User user,String userName) throws IOException {
        User userOther = this.userRepo.findByName(userName);
        Profile profile = userOther.getProfile();
        if(profile.getImagePath() == null){
        Random random = new Random();
        int randomNumber = 1 + random.nextInt(6);
        profile.setImagePath(NAS_BASE_PATH + "sample_"+ randomNumber +".jpg");
        profileRepo.save(profile);
        }
        Path filePath = Paths.get(profile.getImagePath());
        return ResponseEntity.ok(Files.readAllBytes(filePath));
    }

    public String LoadProfileComment_other(User user,String userName){
        User userOther = this.userRepo.findByName(userName);
        Profile profile = userOther.getProfile();
        String profilecomment = profile.getProfilecomment();
        return profilecomment;
    }

    public boolean isUserHaveApplicant(User user,String userId){
        DockerServer userDockerServer = this.dockerServerRepo.findByUser(user);
        List<User> applicantUsers =  this.dockerServerRepo.findApplicantsByDockerServerId(userDockerServer.getId());
        for (User applicant : applicantUsers) {
            if (applicant.getName().equals(userId)) {
                return true; 
            }
        }
        return false;
    }


}
