package com.neo.back.infoManaging.service;

import org.springframework.stereotype.Service;

import com.neo.back.authorization.entity.User;
import com.neo.back.authorization.repository.UserRepository;
import com.neo.back.infoManaging.dto.ManagerPostInquiryDto;
import com.neo.back.infoManaging.dto.UserInquiryListDto;
import com.neo.back.infoManaging.dto.UserInquiryToAnswer;
import com.neo.back.infoManaging.dto.UserPostInquiryDto;
import com.neo.back.infoManaging.entity.UserInquiry;
import com.neo.back.infoManaging.middleware.RootAPI;
import com.neo.back.infoManaging.repository.UserInquiryRepository;
import com.neo.back.service.dto.ServerListDto;

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
                inquiry.getUserInquiryDate()))
                .collect(Collectors.toList());
    }

    public Mono<String> deleteUserInquiry(User user, Long inquiryId){
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

    public Mono<Object> postManagerInquiry(User user,ManagerPostInquiryDto inquiryData){
        if(!this.rootAPI.checkManager(user)) return Mono.just("not Manager");
        Optional<UserInquiry> inquirys = userInquiryRepo.findById(inquiryData.getInquiryId());
        if(inquirys.isPresent()){
            UserInquiry inquiry = inquirys.get();
            String answerTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            inquiry.setManagerAnswer(inquiryData.getInquiry());
            inquiry.setManagerAnswerDate(answerTime);
            inquiry.setAnswerOrNot(true);
            userInquiryRepo.save(inquiry);
            return Mono.just("success to answer userInquiry");
        }
        else{
            return Mono.just("fail to answer userInquiry");
        }
    }

    public Mono<Object> getManagerInquiryToAnswer(User user, Long inquiryId){
        if(!this.rootAPI.checkManager(user)) return Mono.just("not Manager");
        Optional<UserInquiry> inquirys = userInquiryRepo.findById(inquiryId);
        if(inquirys.isPresent()){
            UserInquiry inquiry = inquirys.get();
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
        }
        else{
            return Mono.just("fail to get ManagerInquiry");
        }
    }

    public Mono<Object> getManagerInquiryList(User user){
        if(!this.rootAPI.checkManager(user)) return Mono.just("not Manager");
        List<UserInquiry> inquirys = userInquiryRepo.findAll();
        return Mono.just(inquirys.stream()
        .map(inquiry -> new UserInquiryListDto(
         inquiry.getId(),
         inquiry.getAnswerOrNot(),
         inquiry.getUserInquiryTitle(),
         inquiry.getUserInquiryDate()))
         .collect(Collectors.toList()));
    }

    public Mono<String> deleteManagerInquiry(User user, Long inquiryId){
        if(!this.rootAPI.checkManager(user)) return Mono.just("not Manager");
        Optional<UserInquiry> inquirys = userInquiryRepo.findById(inquiryId);
        if(inquirys.isPresent()){
            userInquiryRepo.deleteById(inquiryId);
            return Mono.just("success to delete Inquiry by manager");
        }
        else{
            return Mono.just("fail to delete Inquiry by manager");
        }
    }

}
