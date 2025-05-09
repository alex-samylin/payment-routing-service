package com.alexsamylin.paymentrouting.provider;

import com.alexsamylin.paymentrouting.dto.PaymentProviderRequestDTO;
import com.alexsamylin.paymentrouting.dto.PaymentResponseDTO;
import com.alexsamylin.paymentrouting.dto.ProviderARawResponse;
import com.alexsamylin.paymentrouting.error.ProviderBusinessException;
import com.alexsamylin.paymentrouting.error.ProviderTechnicalException;
import com.alexsamylin.paymentrouting.model.PaymentErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProviderAClient implements PaymentProvider {

    private final ObjectMapper objectMapper;
    private final ProviderAInvoker invoker;

    @Override
    public String getProviderName() {
        return "ProviderA";
    }

    @Override
    public PaymentResponseDTO process(PaymentProviderRequestDTO request) {
        try {
            ProviderARawResponse body = invoker.call(request);
            log.info("Received successful response from {}: {}", getProviderName(), body);

            return PaymentResponseDTO.builder()
                    .message("SUCCESS")
                    .provider(getProviderName())
                    .transactionId(body.getTransactionId())
                    .build();
        } catch (HttpClientErrorException.Conflict e) {
            log.error("Business error from {}: {} {}", getProviderName(), e.getStatusCode(), e.getResponseBodyAsString());
            ProviderARawResponse error = parseErrorResponse(e.getResponseBodyAsString(), request.getTransactionId());
            throw new ProviderBusinessException(mapProviderErrorCode(error.getErrorCode()), getProviderName(), error.getTransactionId(), error.getMessage());
        } catch (Exception e) {
            log.error("Provider technical failure [{}]: {}", getProviderName(), e.getMessage(), e);
            throw new ProviderTechnicalException("Technical failure from provider: " + getProviderName(), getProviderName(), request.getTransactionId(), e);
        }
    }

    private ProviderARawResponse parseErrorResponse(String json, String transactionId) {
        try {
            return objectMapper.readValue(json, ProviderARawResponse.class);
        } catch (JsonProcessingException parseException) {
            log.error("Failed to parse error response from {}: {}", getProviderName(), parseException.getMessage());
            throw new ProviderTechnicalException("Failed to parse provider error payload", getProviderName(), transactionId, parseException);
        }
    }

    private PaymentErrorCode mapProviderErrorCode(String providerCode) {
        if (providerCode == null) return PaymentErrorCode.UNKNOWN_ERROR;

        return switch (providerCode.toUpperCase()) {
            case "INSUFFICIENT_FUNDS" -> PaymentErrorCode.INSUFFICIENT_FUNDS;
            case "EXPIRED_CARD" -> PaymentErrorCode.EXPIRED_CARD;
            case "INVALID_CURRENCY" -> PaymentErrorCode.INVALID_CURRENCY;
            case "CARD_BLOCKED" -> PaymentErrorCode.CARD_BLOCKED;
            case "LIMIT_EXCEEDED" -> PaymentErrorCode.LIMIT_EXCEEDED;
            default -> PaymentErrorCode.UNKNOWN_ERROR;
        };
    }

}
