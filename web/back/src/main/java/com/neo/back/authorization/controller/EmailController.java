package com.neo.back.authorization.controller;


import com.neo.back.authorization.dto.EmailRequestDTO;
import com.neo.back.authorization.service.EmailService;

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
        return ResponseEntity.ok("인증번호를 보냈습니다. 이메일을 확인해주세요.");
    }

    @PostMapping("/authcode")
    public ResponseEntity<String> sendEmailAndCode(@RequestBody EmailRequestDTO dto) throws NoSuchAlgorithmException {
        if (emailService.verifyEmailCode(dto.getEmail(), dto.getCode())) {


            return ResponseEntity.ok("정상적으로 인증됨.");
        }
        return ResponseEntity.notFound().build();
    }
}