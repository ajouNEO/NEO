package com.neo.back.infoManaging.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInquiryListDto {
    private Long inquiryId;
    private Boolean answerOrNot;
    private String inquiryTitle;
    private String userInquiryDate;
    private String userName;
    private String name;
}
