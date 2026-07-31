package com.sentinel.backend.fraud;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sentinel.backend.entity.Transaction;
import com.sentinel.backend.fraud.rules.FraudRule;

@Service
public class FraudDetectionService {

    private final List<FraudRule> fraudRules;

    public FraudDetectionService(List<FraudRule> fraudRules) {
        this.fraudRules = fraudRules;
    }

    public FraudResult evaluate(Transaction transaction) {
        return fraudRules.stream()
                .map(rule -> rule.evaluate(transaction))
                .max(Comparator.comparingInt(FraudResult::getRiskScore))
                .orElseGet(() -> FraudResult.builder()
                        .riskScore(0)
                        .decision("ALLOW")
                        .reason("No fraud rules configured")
                        .build());
    }
}
