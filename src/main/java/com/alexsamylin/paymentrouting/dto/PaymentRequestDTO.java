package com.alexsamylin.paymentrouting.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentRequestDTO {

    @NotNull
    private Long amount;
    @NotNull
    private String currency;
    @NotNull
    private String cardNumber;
}
