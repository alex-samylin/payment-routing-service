package com.alexsamylin.paymentrouting.dto;

import lombok.Data;

@Data
public class ProviderARawResponse {
    private String status;      // "SUCCESS" or "FAILURE"
    private String errorCode;   // e.g. "INSUFFICIENT_FUNDS"
    private String message;
    private String transactionId;
}
