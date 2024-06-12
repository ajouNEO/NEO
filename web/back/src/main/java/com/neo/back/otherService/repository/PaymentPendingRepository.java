package com.neo.back.otherService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.neo.back.otherService.entity.PaymentPending;

import java.util.List;

@Repository
public interface PaymentPendingRepository extends JpaRepository<PaymentPending,Long> {
    PaymentPending findByTid(String tid);
    PaymentPending findByPartnerOrderId(String PartnerOrderId);

    PaymentPending findByPartnerUserId(String PartnerUserId);

    List<PaymentPending> findAllByPartnerUserId(String userId);


}
