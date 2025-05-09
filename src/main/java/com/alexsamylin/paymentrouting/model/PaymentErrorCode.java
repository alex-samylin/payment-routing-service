package com.alexsamylin.paymentrouting.model;

public enum PaymentErrorCode {
    RATE_LIMITED,
    INSUFFICIENT_FUNDS,
    EXPIRED_CARD,
    INVALID_CURRENCY,
    LIMIT_EXCEEDED,
    CARD_BLOCKED,
    TECHNICAL_ERROR,
    UNKNOWN_ERROR
}
