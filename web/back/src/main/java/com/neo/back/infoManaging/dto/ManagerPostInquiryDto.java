package com.neo.back.infoManaging.dto;

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
