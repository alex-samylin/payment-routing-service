package com.alexsamylin.paymentrouting.provider;

import com.alexsamylin.paymentrouting.dto.PaymentProviderRequestDTO;
import com.alexsamylin.paymentrouting.dto.ProviderARawResponse;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class ProviderAInvoker {

    private final RestTemplate restTemplate;

    @Value("${provider.a.url}")
    private String providerUrl;

    public ProviderAInvoker(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Retry(name = "provider-a")
    public ProviderARawResponse call(PaymentProviderRequestDTO request) {
        log.info("Calling {} with request: {}", providerUrl, request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PaymentProviderRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ProviderARawResponse> response = restTemplate.exchange(
                providerUrl,
                HttpMethod.POST,
                entity,
                ProviderARawResponse.class
        );

        return response.getBody();
    }
}
