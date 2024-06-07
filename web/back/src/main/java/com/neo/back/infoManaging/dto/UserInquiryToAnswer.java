package com.neo.back.infoManaging.dto;

import com.neo.back.authorization.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInquiryToAnswer {
    private Long id;

    private Boolean answerOrNot;

    private String userInquiryTitle;
    private String userInquiry;
    private String userInquiryDate;

    private String managerAnswer;
    private String managerAnswerDate;

    private String userName;
}
