package com.neo.back.authorization.repository;

import com.neo.back.authorization.entity.PaymentCompleted;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentCompletedRepository extends JpaRepository<PaymentCompleted,Long> {
}
