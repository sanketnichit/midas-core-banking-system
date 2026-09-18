package com.sanket.midas.kafka;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BankingEvent(
        Long transactionId,
        String type,
        Long accountId,
        Long recipientAccountId,
        BigDecimal amount,
        String status,
        LocalDateTime timestamp,
        String description
) {
}