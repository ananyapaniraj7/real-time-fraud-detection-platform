
package com.sentinel.backend.fraud.rules;

import java.time.Clock;
import java.time.Instant;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import com.sentinel.backend.entity.Transaction;
import com.sentinel.backend.fraud.FraudResult;

@Component
public class VelocityCheckRule implements FraudRule {

    private static final int VELOCITY_THRESHOLD = 4;
    private static final int VELOCITY_RISK_SCORE = 35;

    private static final Duration WINDOW = Duration.ofSeconds(60);

    private final Clock clock;

    private final ConcurrentMap<String, Deque<Instant>> customerTransactions =
            new ConcurrentHashMap<>();

    // Spring uses this constructor in the application.
    public VelocityCheckRule() {
        this(Clock.systemUTC());
    }

    // This constructor allows deterministic testing with a fixed Clock.
    public VelocityCheckRule(Clock clock) {
        this.clock = clock;
    }

    @Override
public FraudResult evaluate(Transaction transaction) {

    String customerId = transaction.getCustomerId();

    if (customerId == null || customerId.isBlank()) {
        return noRisk();
    }

    Instant now = clock.instant();
    Instant windowStart = now.minus(WINDOW);

    int[] transactionCount = new int[1];

    customerTransactions.compute(
            customerId,
            (key, timestamps) -> {

                if (timestamps == null) {
                    timestamps = new ArrayDeque<>();
                }

                while (!timestamps.isEmpty()
                        && !timestamps.peekFirst().isAfter(windowStart)) {
                    timestamps.removeFirst();
                }

                timestamps.addLast(now);

                transactionCount[0] = timestamps.size();

                return timestamps;
            }
    );

    if (transactionCount[0] >= VELOCITY_THRESHOLD) {
        return FraudResult.builder()
                .riskScore(VELOCITY_RISK_SCORE)
                .reason("High transaction velocity: "
                        + transactionCount[0]
                        + " transactions within 60 seconds")
                .build();
    }

    return noRisk();
}

    private FraudResult noRisk() {
        return FraudResult.builder()
                .riskScore(0)
                .reason("No velocity risk detected")
                .build();
    }
}