package com.neo.back.authorization.service;

import com.neo.back.authorization.util.RedisUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;


import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final RedisUtil redisUtil;

    @Autowired
    private SpringTemplateEngine templateEngine; // 스프링에서 관리하는 TemplateEngine 빈을 주입 받습니다.

    @Value("${spring.mail.username}")
    private String configEmail;

    private String createdCode() {
        int leftLimit = 48; // number '0'
        int rightLimit = 122; // alphabet 'z'
        int targetStringLength = 6;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }


    private String setContext(String code) {
        Context context = new Context();
        context.setVariable("code", code);

        return templateEngine.process("mail", context); // 기존 TemplateEngine 대신 SpringTemplateEngine 사용
    }

    private MimeMessage createEmailForm(String email) throws MessagingException {
        String authCode = createdCode();

        MimeMessage message = mailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject("안녕하세요 인증번호입니다.");
        message.setFrom(configEmail);
        message.setText(setContext(authCode), "utf-8", "html");

        redisUtil.setDataExpire(email, authCode, 60 * 30L);

        return message;
    }


    public void sendEmail(String toEmail) throws MessagingException {
        if (redisUtil.existData(toEmail)) {
            redisUtil.deleteData(toEmail);

        }

        MimeMessage emailForm = createEmailForm(toEmail);

        mailSender.send(emailForm);
    }


    public Boolean verifyEmailCode(String email, String code) {
        String codeFoundByEmail = redisUtil.getData(email);
        System.out.println(codeFoundByEmail);
        if (codeFoundByEmail == null) {
            return false;
        }
        return codeFoundByEmail.equals(code);
    }


    public void sendResetEmail(String toEmail, String tempPassword) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, toEmail);
        message.setSubject("비밀번호 재설정 안내");
        message.setFrom(configEmail);

        // 이메일 내용 설정 (위에 sendemail처럼 템플릿 form 활용 가능 일단은 임시)
        String content = "귀하의 임시 비밀번호는 " + tempPassword + "입니다. 로그인 후 비밀번호를 변경해주세요.";
        message.setText(content, "utf-8", "html");

        mailSender.send(message);
    }

}