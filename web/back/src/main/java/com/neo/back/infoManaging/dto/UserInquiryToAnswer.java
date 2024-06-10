package com.neo.back.infoManaging.dto;

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
