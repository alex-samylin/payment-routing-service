package com.alexsamylin.paymentrouting.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentProviderRequestDTO {

    private String transactionId;
    private Long amount;
    private String currency;
    private String cardNumber;
}
