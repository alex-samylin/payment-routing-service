package com.alexsamylin.paymentrouting.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponseDTO {
    private String provider;
    private String transactionId;
    private String message;
}
