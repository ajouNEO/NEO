package com.neo.back.otherService.entity;

import com.neo.back.authorization.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class UserInquiry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean answerOrNot;

    @Column(length = 100)
    private String userInquiryTitle;
    @Column(length = 700)
    private String userInquiry;
    private String userInquiryDate;

    @ManyToOne 
    @JoinColumn(name = "user_id") 
    private User user;

    @Column(length = 700)
    private String managerAnswer;
    private String managerAnswerDate;
}
