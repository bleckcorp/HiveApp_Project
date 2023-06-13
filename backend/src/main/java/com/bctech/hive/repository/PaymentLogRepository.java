package com.bctech.hive.repository;

import com.bctech.hive.constant.TransactionStatus;
import com.bctech.hive.entity.PaymentLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentLogRepository extends JpaRepository<PaymentLog, String> {
    Optional<PaymentLog> findByTransactionReference(String reference);

    List<PaymentLog> findAllByTransactionStatus(TransactionStatus pending);
}