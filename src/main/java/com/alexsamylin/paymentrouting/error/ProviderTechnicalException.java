package com.alexsamylin.paymentrouting.error;

import lombok.Getter;

@Getter
public class ProviderTechnicalException extends RuntimeException {

    private final String provider;
    private final String transactionId;

    public ProviderTechnicalException(String message, String provider, String transactionId, Throwable cause) {
        super(message, cause);
        this.provider = provider;
        this.transactionId = transactionId;
    }

}

