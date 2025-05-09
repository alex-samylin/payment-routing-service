package com.alexsamylin.paymentrouting.dto;

import lombok.Data;

@Data
public class ProviderBRawResponse {
    private String result;        // "SUCCESS" or "ERROR"
    private String code;          // e.g. "NOT_ENOUGH_BALANCE"
    private String description;
    private String transactionId;
}
