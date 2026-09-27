package com.sentinel.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.sentinel.backend.fraud.FraudDecision;

public record TransactionHistoryResponse(
        UUID id,
        String customerId,
        String merchantId,
        BigDecimal amount,
        String currency,
        Integer riskScore,
        FraudDecision decision,
        String reason,
        Instant createdAt
) {
}