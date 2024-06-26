package com.neo.back.otherService.service;

import com.neo.back.authorization.entity.User;
import com.neo.back.authorization.repository.UserRepository;
import com.neo.back.mainService.dto.UserProfileDto;
import com.neo.back.mainService.entity.DockerServer;
import com.neo.back.mainService.repository.DockerServerRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserInfoService {

    private static final String NAS_BASE_PATH = "/mnt/nas/profileImage/";


    private final UserRepository userRepo;
    private final DockerServerRepository dockerServerRepo;

    public ResponseEntity<String> isAdmin(User user){
        if(user.getRole().equals("ROLE_ADMIN")){
            return ResponseEntity.ok("admin");
        }
        else{
            return ResponseEntity.ok("no admin");
        }

    }

    public ResponseEntity<String> saveProfileImage(User user, MultipartFile file) throws IOException {
        String fileName = user.getName() + ".jpg";
        Path filePath = Paths.get(NAS_BASE_PATH + fileName);
        Files.write(filePath, file.getBytes());
        user.setImagePath(filePath.toString());

        userRepo.save(user);

        return ResponseEntity.ok("success");

    }

    public boolean saveProfileComment(User user, UserProfileDto userProfileDto){
        user.setProfileComment(userProfileDto.getProfilecomment());

        userRepo.save(user);

        return true;
    }

    public byte[] LoadProfileImage(User user) throws IOException {

        if(user.getImagePath() == null){
            Random random = new Random();
            int randomNumber = 1 + random.nextInt(6);
            user.setImagePath(NAS_BASE_PATH + "sample_"+ randomNumber +".jpg");
            userRepo.save(user);
        }
        Path filePath = Paths.get(user.getImagePath());

        return Files.readAllBytes(filePath);
    }

    public String LoadProfileComment(User user){
        String profilecomment = user.getProfileComment();

        return profilecomment;

    }

    public String LoadProfileEmail(User user) {
        String Email = user.getEmail();

        return Email;
    }

    public ResponseEntity<Object> getProfileImage_other(User user,String userName) throws IOException {
        User userOther = this.userRepo.findByName(userName);
        if(userOther.getImagePath() == null){
            Random random = new Random();
            int randomNumber = 1 + random.nextInt(6);
            userOther.setImagePath(NAS_BASE_PATH + "sample_"+ randomNumber +".jpg");
            userRepo.save(userOther);
        }
        Path filePath = Paths.get(userOther.getImagePath());
        return ResponseEntity.ok(Files.readAllBytes(filePath));
    }

    public String LoadProfileComment_other(User user,String userName){
        User userOther = this.userRepo.findByName(userName);
        String profilecomment = userOther.getProfileComment();
        return profilecomment;
    }

    public boolean isUserHaveApplicant(User user,String userId){
        DockerServer userDockerServer = this.dockerServerRepo.findByUser(user);
        List<User> applicantUsers =  this.dockerServerRepo.findApplicantsByDockerServerId(userDockerServer.getId());
        List<User> participants =  this.dockerServerRepo.findParticipantsByDockerServerId(userDockerServer.getId());
        for (User applicant : applicantUsers) {
            if (applicant.getName().equals(userId)) {
                return true; 
            }
        }
        for (User participant : participants) {
            if (participant.getName().equals(userId)) {
                return true; 
            }
        }
        return false;
    }


}
