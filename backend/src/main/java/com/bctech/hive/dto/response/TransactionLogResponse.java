package com.bctech.hive.dto.response;

import com.bctech.hive.constant.TransactionStatus;
import com.bctech.hive.constant.TransactionType;
import com.bctech.hive.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionLogResponse {
    private BigDecimal amount;
    private TransactionType transactionType;
    private String transactionDate;
}
