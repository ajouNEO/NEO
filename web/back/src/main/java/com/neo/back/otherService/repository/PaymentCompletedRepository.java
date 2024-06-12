package com.neo.back.otherService.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.neo.back.otherService.entity.PaymentCompleted;

import java.util.List;


public interface PaymentCompletedRepository extends JpaRepository<PaymentCompleted,Long> {

    List<PaymentCompleted> findAllByPartnerUserId(String userId);


}
