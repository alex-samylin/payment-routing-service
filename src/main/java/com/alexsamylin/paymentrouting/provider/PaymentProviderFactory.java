package com.alexsamylin.paymentrouting.provider;

import com.alexsamylin.paymentrouting.dto.PaymentRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentProviderFactory {

    private final ProviderAClient providerA;
    private final ProviderBClient providerB;

    public PaymentProvider resolve(PaymentRequestDTO request) {
        String bin = request.getCardNumber().substring(0, 6);
        String currency = request.getCurrency();
        long amount = request.getAmount();

        if (bin.startsWith("42") && currency.equals("USD") && amount < 100000) {
            return providerA;
        } else {
            return providerB;
        }
    }
}
