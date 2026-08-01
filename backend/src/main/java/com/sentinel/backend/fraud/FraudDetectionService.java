package com.sentinel.backend.fraud;

import java.util.stream.Collectors;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sentinel.backend.entity.Transaction;
import com.sentinel.backend.fraud.rules.FraudRule;

@Service
public class FraudDetectionService {
    private static final int MAX_RISK_SCORE = 100;
    private static final int REVIEW_THRESHOLD = 30;
    private static final int BLOCK_THRESHOLD = 70;
    private final List<FraudRule> fraudRules;

    public FraudDetectionService(List<FraudRule> fraudRules) {
        this.fraudRules = fraudRules;
    }

    public FraudResult evaluate(Transaction transaction) {
        List<FraudResult> results = fraudRules.stream()
                .map(rule -> rule.evaluate(transaction))
                .toList();

        int riskScore = clampRiskScore(results.stream()
                .mapToInt(FraudResult::getRiskScore)
                .sum());

        if (riskScore == 0) {
            return FraudResult.builder()
                    .riskScore(0)
                    .decision(FraudDecision.ALLOW)
                    .reason("No fraud indicators detected")
                    .build();
        }

        String reason = results.stream()
                .filter(result -> result.getRiskScore() > 0)
                .map(FraudResult::getReason)
                .collect(Collectors.joining(", "));

        return FraudResult.builder()
                .riskScore(riskScore)
                .decision(resolveDecision(riskScore))
                .reason(reason)
                .build();
    }

    private int clampRiskScore(int riskScore) {
        return Math.min(riskScore, MAX_RISK_SCORE);
    }

    private FraudDecision resolveDecision(int riskScore) {
        if (riskScore >= BLOCK_THRESHOLD) {
            return FraudDecision.BLOCK;
        }

        if (riskScore >= REVIEW_THRESHOLD) {
            return FraudDecision.REVIEW;
        }

        return FraudDecision.ALLOW;
    }
}
