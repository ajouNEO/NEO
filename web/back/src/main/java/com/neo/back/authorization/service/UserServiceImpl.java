package com.neo.back.authorization.service;

import com.neo.back.authorization.entity.PaymentCompleted;
import com.neo.back.authorization.entity.PaymentPending;
import com.neo.back.authorization.entity.User;
import com.neo.back.authorization.repository.PaymentCompletedRepository;
import com.neo.back.authorization.repository.PaymentPendingRepository;
import com.neo.back.authorization.repository.UserRepository;
import com.neo.back.mainService.repository.DockerImageRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCompletedRepository paymentCompletedRepository;

    @Autowired
    private PaymentPendingRepository paymentPendingRepository;

    @Autowired
    private DockerImageRepository dockerImageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;

    @Override
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username);
        if (user != null && passwordEncoder.matches(currentPassword, user.getPassword())) {

            System.out.println(user.getPassword());
            System.out.println(currentPassword);
            System.out.println(newPassword);
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }
        return false;

    }

    @Override
    public boolean resetPassword(String username) {
        User user = userRepository.findByUsername(username);

        if (user != null) {
            String tempPassword = createTemporaryPassword(); // 임시 비밀번호 생성 메소드
            user.setPassword(passwordEncoder.encode(tempPassword));
            userRepository.save(user);

            try {
                emailService.sendResetEmail(user.getEmail(),tempPassword);
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean checkDuplicateEmail(String email) {
        if(email == null || email.trim().isEmpty()) {
            // 유효하지 않은 username 입력 처리
            return false;
        }
        return !userRepository.existsByUsername(email);
    }

    @Override
    public boolean checkDuplicateName(String name) {
        if(name == null || name.trim().isEmpty()) {
            // 유효하지 않은 username 입력 처리
            return false;
        }

        return !userRepository.existsByname(name);
    }

    @Override
    public ResponseEntity<String> changenickname(User user, String name) {
        if(name == null || name.trim().isEmpty()) {
            // 유효하지 않은 username 입력 처리
            return ResponseEntity.badRequest().body("닉네임이 유효하지 않습니다.");
        }

        if (userRepository.existsByname(name)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 있는 닉네임입니다.");
        }

        user.setName(name);
        userRepository.save(user);

        return ResponseEntity.ok("닉네임이 성공적으로 변경되었습니다.");
    }

    @Override
    @Transactional
    public void deleteUser(User user) {

        // PaymentCompleted 기록 조회 및 삭제 혹은 처리
        List<PaymentCompleted> payments = paymentCompletedRepository.findAllByPartnerUserId(user.getId().toString());
        for (PaymentCompleted payment : payments) {
            // 여기서는 단순 삭제를 예로 들지만, 실제로는 다른 처리를 할 수도 있습니다.
            paymentCompletedRepository.delete(payment);
        }

        List<PaymentPending> pendingpayments = paymentPendingRepository.findAllByPartnerUserId(user.getId().toString());
        for (PaymentPending payment : pendingpayments) {
            // 여기서는 단순 삭제를 예로 들지만, 실제로는 다른 처리를 할 수도 있습니다.
            paymentPendingRepository.delete(payment);
        }


        dockerImageRepository.deleteByUserId(user.getId());

        userRepository.delete(user);
        return;
    }



    private String createTemporaryPassword() {
        // 임시 비밀번호 생성 로직 구현
        // 예시: 8자리의 랜덤 문자열 생성
        String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        String NUMBER = "0123456789";
        String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());
            char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);
            sb.append(rndChar);
        }
        return sb.toString();
    }

}
