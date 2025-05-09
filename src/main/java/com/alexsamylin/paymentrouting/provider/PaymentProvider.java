package com.alexsamylin.paymentrouting.provider;

import com.alexsamylin.paymentrouting.dto.PaymentProviderRequestDTO;
import com.alexsamylin.paymentrouting.dto.PaymentResponseDTO;

public interface PaymentProvider {
    String getProviderName();
    PaymentResponseDTO process(PaymentProviderRequestDTO request);
}
