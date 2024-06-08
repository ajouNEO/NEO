package com.neo.back.authorization.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neo.back.authorization.dto.OrderRequestDTO;
import com.neo.back.authorization.entity.PaymentPending;
import com.neo.back.authorization.entity.PointProduct;
import com.neo.back.authorization.entity.Product;
import com.neo.back.authorization.entity.User;
import com.neo.back.authorization.repository.PaymentPendingRepository;
import com.neo.back.authorization.repository.ProductRepository;
import com.neo.back.authorization.service.KakaoPayService;
import com.neo.back.service.utility.GetCurrentUser;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequiredArgsConstructor
public class PayController {

    private final KakaoPayService kakaoPayService;
    private final AtomicInteger orderCounter = new AtomicInteger(0); // 오늘 생성된 주문 숫자 카운트


    private final ProductRepository productRepository;
    private final PaymentPendingRepository paymentPendingRepository;
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
                       // String nextRedirectPcUrl = jsonNode.get("next_redirect_pc_url").asText();

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
                    // 결제 승인 후 로직 추가 가능

                    System.out.println("성공!!!!!!!!!");
                    return approveResponse;
                });
    }

    /*private String extractPgToken(String url) {
        String pattern = "pg_token=(.+)";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(url);
        if (m.find()) {
            return m.group(1);
        }
        throw new IllegalArgumentException("Invalid next_redirect_pc_url format");
    }*/

    private String generatePartnerOrderId() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = now.format(formatter);
        int orderNumber = orderCounter.incrementAndGet();
        return String.format("%s%03d", timestamp, orderNumber);
    }

}
