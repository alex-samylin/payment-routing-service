package com.alexsamylin.paymentrouting.service;

import com.alexsamylin.paymentrouting.dto.PaymentProviderRequestDTO;
import com.alexsamylin.paymentrouting.dto.PaymentRequestDTO;
import com.alexsamylin.paymentrouting.dto.PaymentResponseDTO;
import com.alexsamylin.paymentrouting.provider.PaymentProvider;
import com.alexsamylin.paymentrouting.provider.PaymentProviderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentProviderFactory providerFactory;

    public PaymentResponseDTO processPayment(PaymentRequestDTO request) {
        String transactionId = UUID.randomUUID().toString();
        PaymentProviderRequestDTO providerRequestDTO = toPaymentProviderRequestDTO(request, transactionId);

        PaymentProvider provider = providerFactory.resolve(request);
        log.info("Processing transaction [{}] using provider [{}]", transactionId, provider.getProviderName());

        return provider.process(providerRequestDTO);
    }

    private PaymentProviderRequestDTO toPaymentProviderRequestDTO(PaymentRequestDTO request, String transactionId) {
        return PaymentProviderRequestDTO.builder()
                .transactionId(transactionId)
                .cardNumber(request.getCardNumber())
                .currency(request.getCurrency())
                .amount(request.getAmount())
                .build();
    }
}
