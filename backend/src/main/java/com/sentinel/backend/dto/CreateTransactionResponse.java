package com.sentinel.backend.dto;

import com.sentinel.backend.fraud.FraudDecision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class CreateTransactionResponse {
    private String message;
    private int riskScore;
    private FraudDecision decision;
    private String reason;
}
