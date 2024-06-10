package com.neo.back.otherService.dto;

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
