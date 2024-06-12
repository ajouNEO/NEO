package com.neo.back.otherService.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.neo.back.authorization.dto.OrderRequestDTO;
import com.neo.back.authorization.entity.*;
import com.neo.back.otherService.entity.PaymentCompleted;
import com.neo.back.otherService.entity.PaymentPending;
import com.neo.back.otherService.repository.PaymentCompletedRepository;
import com.neo.back.otherService.repository.PaymentPendingRepository;
import com.neo.back.otherService.service.KakaoPayService;
import com.neo.back.utility.GetCurrentUser;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequiredArgsConstructor
public class PayController {

    private final KakaoPayService kakaoPayService;
    private final AtomicInteger orderCounter = new AtomicInteger(0); // 오늘 생성된 주문 숫자 카운트


    private final PaymentPendingRepository paymentPendingRepository;

    private final PaymentCompletedRepository paymentCompletedRepository;
    private final GetCurrentUser getCurrentUser;

    private final ObjectMapper objectMapper;

    @PostMapping("/kakaoPay")
    public Mono<String> kakaoPay(@RequestBody OrderRequestDTO orderRequestDTO, HttpSession session) {

        String itemname = "point";
        User user = getCurrentUser.getUser();
        Integer productprice = orderRequestDTO.getTotal_amount();
        Integer taxprice =0;
        String partner_user_id = String.valueOf(user.getId());
        String partner_order_id = generatePartnerOrderId();

        // startPayment 호출 후 응답을 받을 때까지 블록

        return kakaoPayService.startPayment(user, partner_order_id, partner_user_id, itemname, 1, productprice, taxprice, 0)
                .map(response -> {
                    try {
                        JsonNode jsonNode = objectMapper.readTree(response);
                        String tid = jsonNode.get("tid").asText();
                            // 결제 대기 정보를 데이터베이스에 저장
                            PaymentPending paymentPending = new PaymentPending();
                            paymentPending.setTid(tid);
                            paymentPending.setPartnerOrderId(partner_order_id);
                            paymentPending.setPartnerUserId(partner_user_id);
                            paymentPending.setItemName(itemname);
                            paymentPending.setQuantity(1);
                            paymentPending.setTotalAmount(productprice);
                            paymentPending.setVatAmount(taxprice);
                            paymentPending.setPaymentDate(LocalDateTime.now());
                            paymentPending.setUsername(user.getUsername());
                            paymentPendingRepository.save(paymentPending);

                        session.setAttribute("tid", tid);
                        session.setAttribute("partnerOrderId", partner_order_id);
                        session.setAttribute("partnerUserId", partner_user_id);


                        return response; // 클라이언트에게 redirect URL을 전달
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Parsing error", e);
                    }
                });
    }
    @GetMapping("/kakaoPay/success")
    public Mono<String> kakaoPaySuccess(@RequestParam("pg_token") String pgToken,@RequestParam("partner_user_id") String partnerUserId
            ,HttpSession session) {
        PaymentPending paymentPending =  paymentPendingRepository.findByPartnerUserId(partnerUserId);

        String partnerOrderId =paymentPending.getPartnerOrderId();
        String tid = paymentPending.getTid();

        System.out.println(session.getId());
        System.out.println(tid);
        System.out.println(partnerOrderId);
        System.out.println(partnerUserId);

        if (tid == null || partnerOrderId == null || partnerUserId == null) {
            return Mono.error(new RuntimeException("Session attributes are missing"));
        }

        return kakaoPayService.approvePayment(tid, partnerOrderId, partnerUserId, pgToken)
                .map(approveResponse -> {
                    return approveResponse;
                });
    }

    @GetMapping("/api/user/payment")
    public Mono<ResponseEntity<String>> getPaymentList() {
        User user = getCurrentUser.getUser();
        Long userId = user.getId();

        return Mono.fromSupplier(() -> {
            List<PaymentCompleted> paymentList = paymentCompletedRepository.findAllByPartnerUserId(userId.toString());
            if (paymentList.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
                String jsonResponse = objectMapper.writeValueAsString(paymentList);
                return ResponseEntity.ok(jsonResponse);
            } catch (JsonProcessingException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing JSON response");
            }
        });
    }

    private String generatePartnerOrderId() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = now.format(formatter);
        int orderNumber = orderCounter.incrementAndGet();
        return String.format("%s%03d", timestamp, orderNumber);
    }

}
