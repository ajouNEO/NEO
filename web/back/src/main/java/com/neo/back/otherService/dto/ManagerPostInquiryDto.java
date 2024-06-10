package com.neo.back.otherService.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ManagerPostInquiryDto {
    private Long inquiryId;
    private String inquiry;
}
