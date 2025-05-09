package com.alexsamylin.paymentrouting.error;

import com.alexsamylin.paymentrouting.model.PaymentErrorCode;
import lombok.Getter;

@Getter
public class ProviderBusinessException extends RuntimeException {
    private final PaymentErrorCode errorCode;
    private final String provider;
    private final String transactionId;

    public ProviderBusinessException(PaymentErrorCode errorCode, String provider, String transactionId, String message) {
        super(message);
        this.provider = provider;
        this.transactionId = transactionId;
        this.errorCode = errorCode;
    }

}

