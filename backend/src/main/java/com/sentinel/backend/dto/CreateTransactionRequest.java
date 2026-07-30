package com.sentinel.backend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequest {

    @NotBlank
    private String customerId;

    @NotBlank
    private String merchantId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;
}
