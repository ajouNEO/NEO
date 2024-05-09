package com.neo.back.member.controller;


import com.neo.back.member.dto.EmailRequestDTO;
import com.neo.back.member.service.EmailService;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.security.NoSuchAlgorithmException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/email")
public class EmailController {

    private final EmailService emailService;

    @GetMapping("/{email_addr}/authcode")
    public ResponseEntity<String> sendEmailPath(@PathVariable String email_addr) throws MessagingException {
        emailService.sendEmail(email_addr);
        return ResponseEntity.ok("이메일을 확인하세요");
    }

    @PostMapping("/{email_addr}/authcode")
    public ResponseEntity<String> sendEmailAndCode(@RequestBody EmailRequestDTO dto) throws NoSuchAlgorithmException {
        if (emailService.verifyEmailCode(dto.getEmail(), dto.getCode())) {


            return ResponseEntity.ok("정상적으로 인증됨.");
        }
        return ResponseEntity.notFound().build();
    }
}