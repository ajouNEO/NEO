package com.neo.back.infoManaging.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.neo.back.authorization.entity.User;
import com.neo.back.authorization.repository.UserRepository;
import com.neo.back.infoManaging.dto.UserManagingListDto;
import com.neo.back.infoManaging.dto.UserManagingPointDto;

import lombok.RequiredArgsConstructor;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserManagingService {

    private final UserRepository userRepo;
    
    public ResponseEntity<Object> getUserLsitByManager(User user){
        List<User> Users = userRepo.findAllByIdNot((long) 1);

        return ResponseEntity.ok(Users.stream()
        .map(userData -> new UserManagingListDto(
            userData.getUsername(),
            userData.getName(),
            userData.getPoints(),
            userData.getId()))
        .collect(Collectors.toList()));
    }

    public ResponseEntity<Object> addPointToUser(User user, UserManagingPointDto userData){
        Optional<User> targetUsers = userRepo.findById(userData.getId());

        if(targetUsers.isPresent()){
            User targetUser = targetUsers.get();
            targetUser.setPoints(targetUser.getPoints() + userData.getPoints());
            this.userRepo.save(targetUser);
            return ResponseEntity.ok("success to add point");
        }
        else{
            return ResponseEntity.ok("fail to add point");
        }
    }

    public ResponseEntity<Object> subPointToUser(User user, UserManagingPointDto userData){
        Optional<User> targetUsers = userRepo.findById(userData.getId());

        if(targetUsers.isPresent()){
            User targetUser = targetUsers.get();
            targetUser.setPoints(targetUser.getPoints() - userData.getPoints());
            this.userRepo.save(targetUser);
            return ResponseEntity.ok("success to subtract point");
        }
        else{
            return ResponseEntity.ok("fail to subtract point");
        }
    }

    public ResponseEntity<Object> stopUserAccount(User user, Long userId){
        Optional<User> targetUsers = userRepo.findById(userId);
        if(targetUsers.isPresent()){
            User targetUser = targetUsers.get();
            targetUser.setAccountStatus(true);
            this.userRepo.save(targetUser);
            return ResponseEntity.ok("success to stop user account");
        }
        else{
            return ResponseEntity.ok("fail to stop user account");
        }
    }

    public ResponseEntity<Object> activeUserAccount(User user, Long userId){
        Optional<User> targetUsers = userRepo.findById(userId);
        if(targetUsers.isPresent()){
            User targetUser = targetUsers.get();
            targetUser.setAccountStatus(false);
            this.userRepo.save(targetUser);
            return ResponseEntity.ok("success to active user account");
        }
        else{
            return ResponseEntity.ok("fail to active user account");
        }
    }
}
