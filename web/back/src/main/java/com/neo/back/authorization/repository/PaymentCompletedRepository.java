package com.neo.back.authorization.repository;

import com.neo.back.authorization.entity.PaymentCompleted;
import org.springframework.data.jpa.repository.JpaRepository;
import reactor.core.publisher.Flux;

import java.util.List;


public interface PaymentCompletedRepository extends JpaRepository<PaymentCompleted,Long> {

    List<PaymentCompleted> findAllByPartnerUserId(String userId);


}
