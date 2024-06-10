package com.neo.back.otherService.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.neo.back.authorization.entity.User;
import com.neo.back.otherService.dto.ManagerPostInquiryDto;
import com.neo.back.otherService.dto.UserInquiryListDto;
import com.neo.back.otherService.dto.UserInquiryToAnswer;
import com.neo.back.otherService.dto.UserPostInquiryDto;
import com.neo.back.otherService.entity.UserInquiry;
import com.neo.back.otherService.middleware.RootAPI;
import com.neo.back.otherService.repository.UserInquiryRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class UserInquiryService {
    private final UserInquiryRepository userInquiryRepo;
    private final RootAPI rootAPI;

    public Mono<Object> postUserInquiry(User user,UserPostInquiryDto inquiryData){
        UserInquiry inquiry = new UserInquiry();
        String startTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        inquiry.setAnswerOrNot(false);

        inquiry.setUser(user);
        inquiry.setUserInquiry(inquiryData.getInquiry());
        inquiry.setUserInquiryTitle(inquiryData.getInquiryTitle());
        inquiry.setUserInquiryDate(startTime.toString());
        
        userInquiryRepo.save(inquiry);
        return Mono.just("success saving inquiry");
    }

    public Mono<Object> getUserInquiryToAnswer(User user, Long inquiryId){
        Optional<UserInquiry> inquirys = userInquiryRepo.findById(inquiryId);
        if(inquirys.isPresent()){
            UserInquiry inquiry = inquirys.get();
            if(!this.rootAPI.checkUser(user,inquiry)) return Mono.just("not your inquiry");
            return Mono.just(new UserInquiryToAnswer(
                inquiry.getId(),
                inquiry.getAnswerOrNot(),
                inquiry.getUserInquiryTitle(),
                inquiry.getUserInquiry(),
                inquiry.getUserInquiryDate(),
                inquiry.getManagerAnswer(),
                inquiry.getManagerAnswerDate(),
                inquiry.getUser().getName()
            ));
        } else {
            return Mono.just("fail to get InquiryToAnswer");
        }
    }

    public List<UserInquiryListDto> getUserInquiryList(User user){
        List<UserInquiry> inquirys = userInquiryRepo.findByUser(user);
        return inquirys.stream()
               .map(inquiry -> new UserInquiryListDto(
                inquiry.getId(),
                inquiry.getAnswerOrNot(),
                inquiry.getUserInquiryTitle(),
                inquiry.getUserInquiryDate(),
                inquiry.getUser().getUsername(),
                inquiry.getUser().getName()
                ))
                .collect(Collectors.toList());
    }

    public Mono<Object> deleteUserInquiry(User user, Long inquiryId){
        Optional<UserInquiry> inquirys = userInquiryRepo.findById(inquiryId);
        if(inquirys.isPresent()){
            UserInquiry inquiry = inquirys.get();
            if(!this.rootAPI.checkUser(user,inquiry)) return Mono.just("not your inquiry");
            userInquiryRepo.deleteById(inquiryId);
            return Mono.just("success to delete Inquiry");
        }
        else{
            return Mono.just("fail to delete Inquiry");
        }
    }

    public ResponseEntity<Object> postManagerInquiry(User user,ManagerPostInquiryDto inquiryData){
        Optional<UserInquiry> inquirys = userInquiryRepo.findById(inquiryData.getInquiryId());
        if(inquirys.isPresent()){
            UserInquiry inquiry = inquirys.get();
            String answerTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            inquiry.setManagerAnswer(inquiryData.getInquiry());
            inquiry.setManagerAnswerDate(answerTime);
            inquiry.setAnswerOrNot(true);
            userInquiryRepo.save(inquiry);
            return ResponseEntity.ok("success to answer userInquiry");
        }
        else{
            return ResponseEntity.ok("fail to answer userInquiry");
        }
    }

    public ResponseEntity<Object> getManagerInquiryToAnswer(User user, Long inquiryId){
        Optional<UserInquiry> inquirys = userInquiryRepo.findById(inquiryId);
        if(inquirys.isPresent()){
            UserInquiry inquiry = inquirys.get();
            return ResponseEntity.ok(new UserInquiryToAnswer(
                inquiry.getId(),
                inquiry.getAnswerOrNot(),
                inquiry.getUserInquiryTitle(),
                inquiry.getUserInquiry(),
                inquiry.getUserInquiryDate(),
                inquiry.getManagerAnswer(),
                inquiry.getManagerAnswerDate(),
                inquiry.getUser().getName()
            ));
        }
        else{
            return ResponseEntity.ok("fail to get ManagerInquiry");
        }
    }

    public ResponseEntity<Object> getManagerInquiryList(User user){
        List<UserInquiry> inquirys = userInquiryRepo.findAll();
        return ResponseEntity.ok(inquirys.stream()
        .map(inquiry -> new UserInquiryListDto(
         inquiry.getId(),
         inquiry.getAnswerOrNot(),
         inquiry.getUserInquiryTitle(),
         inquiry.getUserInquiryDate(),
         inquiry.getUser().getUsername(),
         inquiry.getUser().getName()
         ))
         .collect(Collectors.toList()));
    }

    public ResponseEntity<Object> deleteManagerInquiry(User user, Long inquiryId){
        Optional<UserInquiry> inquirys = userInquiryRepo.findById(inquiryId);
        if(inquirys.isPresent()){
            userInquiryRepo.deleteById(inquiryId);
            return ResponseEntity.ok("success to delete Inquiry by manager");
        }
        else{
            return ResponseEntity.ok("fail to delete Inquiry by manager");
        }
    }

}
