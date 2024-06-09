package com.neo.back.authorization.repository;

import com.neo.back.authorization.entity.PaymentCompleted;
import com.neo.back.authorization.entity.PaymentPending;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentPendingRepository extends JpaRepository<PaymentPending,Long> {
    PaymentPending findByTid(String tid);
    PaymentPending findByPartnerOrderId(String PartnerOrderId);

    PaymentPending findByPartnerUserId(String PartnerUserId);

    List<PaymentPending> findAllByPartnerUserId(String userId);


}
