package com.neo.back.authorization.service;

import com.neo.back.authorization.entity.PaymentCompleted;
import com.neo.back.authorization.entity.PaymentPending;
import com.neo.back.authorization.entity.User;
import com.neo.back.authorization.repository.PaymentCompletedRepository;
import com.neo.back.authorization.repository.PaymentPendingRepository;
import com.neo.back.authorization.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class KakaoPayService {

    private WebClient webClient;

    private PaymentPendingRepository paymentPendingRepository;

    private PaymentCompletedRepository paymentCompletedRepository;

    private UserRepository userRepository;
    @Value("${pay.secret_key}")
    private String secretKey;

    @Value("${MAIN_SERVER_IP}")
	private String mainServerIp;


    public KakaoPayService(WebClient.Builder webClientBuilder ,PaymentPendingRepository paymentPendingRepository,
        PaymentCompletedRepository paymentCompletedRepository,UserRepository userRepository) {
            
        this.webClient = webClientBuilder.baseUrl("https://open-api.kakaopay.com").build();
        this.paymentCompletedRepository = paymentCompletedRepository;
        this.paymentPendingRepository =paymentPendingRepository;
        this.userRepository = userRepository;
    }

    public Mono<String> startPayment(User user,String partner_order_id,String partner_user_id, String itemName, Integer quantity, Integer totalAmount, Integer vatAmount,Integer tax_free_amount ){

        String approvalUrl = String.format("https://%s:8080/kakaoPay/success?partner_user_id=%s",mainServerIp ,partner_user_id);
        String failUrl = String.format("https://%s:8080/", mainServerIp);
        String cancelUrl = String.format("https://%s:8080/", mainServerIp);

        return webClient.post()
                .uri("/online/v1/payment/ready")
                .header("Authorization","SECRET_KEY " + secretKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(String.format("{\n" +
                        "    \"cid\": \"TC0ONETIME\",\n" +
                        " \"partner_order_id\": \"" + partner_order_id + "\",\n" +
                        " \"partner_user_id\": \"" + partner_user_id + "\",\n" +
                        "    \"item_name\": \"" + itemName + "\",\n" +
                        "    \"quantity\": " + quantity + ",\n" +
                        "    \"total_amount\": " + totalAmount + ",\n" +
                        "    \"tax_free_amount\": " +  tax_free_amount + ",\n" +
                        "    \"vat_amount\": " + vatAmount + ",\n" +
                        "    \"approval_url\": \"%s\",\n" +
                        "    \"fail_url\": \"%s\",\n" +
                        "    \"cancel_url\": \"%s\"\n" +
                        "}",approvalUrl,failUrl,cancelUrl))
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> {


                });
    }

    public Mono<String> approvePayment(String tid, String partner_order_id, String partner_user_id, String pgtoken) {
        return webClient.post()
                .uri("/online/v1/payment/approve")
                .header("Authorization", "SECRET_KEY " + secretKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(String.format("{\n" +
                        "\"cid\": \"TC0ONETIME\",\n" +
                        "\"tid\": \"%s\",\n" +
                        "\"partner_order_id\": \"%s\",\n" +
                        "\"partner_user_id\": \"%s\",\n" +
                        "\"pg_token\": \"%s\"\n" + // 마지막 속성 뒤 쉼표 제거
                        "}", tid, partner_order_id, partner_user_id, pgtoken))
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> {
                    PaymentPending paymentPending = paymentPendingRepository.findByPartnerOrderId(partner_order_id);
                    System.out.println(paymentPending);
                    if (paymentPending != null) {
                        PaymentCompleted paymentCompleted = new PaymentCompleted();
                        paymentCompleted.setTid(tid);
                        paymentCompleted.setPartnerOrderId(paymentPending.getPartnerOrderId());
                        paymentCompleted.setPartnerUserId(paymentPending.getPartnerUserId());
                        paymentCompleted.setItemName(paymentPending.getItemName());
                        paymentCompleted.setQuantity(paymentPending.getQuantity());
                        paymentCompleted.setTotalAmount(paymentPending.getTotalAmount());
                        paymentCompleted.setVatAmount(paymentPending.getVatAmount());
                        paymentCompleted.setTaxFreeAmount(paymentPending.getTaxFreeAmount());
                        paymentCompleted.setPaymentDate(LocalDateTime.now());

                        paymentCompletedRepository.save(paymentCompleted);
                        User user = userRepository.findById(Long.valueOf(paymentPending.getPartnerUserId()));
                        user.addPoint(Long.valueOf(paymentPending.getTotalAmount()));
                        userRepository.save(user);
                        paymentPendingRepository.delete(paymentPending);



                        System.out.println("success");
                    }
                    });





    }









}
