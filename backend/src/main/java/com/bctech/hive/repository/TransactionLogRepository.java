package com.bctech.hive.repository;

import com.bctech.hive.constant.TransactionStatus;
import com.bctech.hive.entity.TransactionLog;
import com.bctech.hive.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLog, String> {
    List<TransactionLog> findAllByUserAndTransactionStatusOrderByUpdatedDate(User user, TransactionStatus transactionStatus);
}
