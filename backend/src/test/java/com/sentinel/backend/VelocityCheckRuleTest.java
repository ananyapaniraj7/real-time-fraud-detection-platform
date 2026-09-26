
package com.sentinel.backend;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sentinel.backend.entity.Transaction;
import com.sentinel.backend.fraud.FraudResult;
import com.sentinel.backend.fraud.rules.VelocityCheckRule;

class VelocityCheckRuleTest {

    private MutableClock clock;
    private VelocityCheckRule rule;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(
                Instant.parse("2026-09-26T10:00:00Z")
        );

        rule = new VelocityCheckRule(clock);
    }

    @Test
    void shouldNotFlagFirstThreeTransactions() {

        for (int i = 0; i < 3; i++) {
            FraudResult result = rule.evaluate(transaction("CUSTOMER001"));

            assertThat(result.getRiskScore()).isZero();
        }
    }

    @Test
    void shouldFlagFourthTransactionWithinSixtySeconds() {

        for (int i = 0; i < 3; i++) {
            rule.evaluate(transaction("CUSTOMER001"));
        }

        FraudResult result = rule.evaluate(transaction("CUSTOMER001"));

        assertThat(result.getRiskScore()).isEqualTo(35);
        assertThat(result.getReason())
                .contains("High transaction velocity")
                .contains("4 transactions");
    }

    @Test
    void shouldIgnoreTransactionsOutsideRollingWindow() {

        for (int i = 0; i < 3; i++) {
            rule.evaluate(transaction("CUSTOMER001"));
        }

        // Move forward beyond the 60-second window.
        clock.advance(Duration.ofSeconds(61));

        FraudResult result = rule.evaluate(transaction("CUSTOMER001"));

        assertThat(result.getRiskScore()).isZero();
    }

    @Test
    void shouldTrackDifferentCustomersIndependently() {

        for (int i = 0; i < 3; i++) {
            rule.evaluate(transaction("CUSTOMER001"));
        }

        FraudResult result = rule.evaluate(transaction("CUSTOMER002"));

        assertThat(result.getRiskScore()).isZero();
    }

    @Test
    void shouldHandleConcurrentTransactionsForSameCustomer() {

        int totalTransactions = 20;

        long flaggedTransactions = IntStream.range(0, totalTransactions)
                .parallel()
                .mapToObj(i -> rule.evaluate(transaction("CUSTOMER001")))
                .filter(result -> result.getRiskScore() == 35)
                .count();

        // First 3 are below threshold. Remaining 17 are flagged.
        assertThat(flaggedTransactions).isEqualTo(17);
    }

    private Transaction transaction(String customerId) {
        return Transaction.builder()
                .customerId(customerId)
                .merchantId("MERCHANT001")
                .amount(new BigDecimal("1000.00"))
                .currency("INR")
                .build();
    }

    private static class MutableClock extends Clock {

        private final AtomicReference<Instant> currentInstant;

        MutableClock(Instant initialInstant) {
            this.currentInstant = new AtomicReference<>(initialInstant);
        }

        void advance(Duration duration) {
            currentInstant.updateAndGet(instant -> instant.plus(duration));
        }

        @Override
        public Instant instant() {
            return currentInstant.get();
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }
}