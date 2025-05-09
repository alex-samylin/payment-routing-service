package com.alexsamylin.paymentrouting.controller;

import com.alexsamylin.paymentrouting.dto.PaymentRequestDTO;
import com.alexsamylin.paymentrouting.dto.PaymentResponseDTO;
import com.alexsamylin.paymentrouting.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payments", description = "Payment processing endpoints")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(
            summary = "Create a payment",
            description = "Processes a payment through the appropriate provider based on routing logic"
    )
    @ApiResponse(responseCode = "200", description = "Payment processed successfully")
    @ApiResponse(responseCode = "409", description = "Business error (INSUFFICIENT_FUNDS/EXPIRED_CARD/CARD_BLOCKED/etc)")
    @ApiResponse(responseCode = "502", description = "Technical failure from provider")
    @PostMapping
    public ResponseEntity<PaymentResponseDTO> createPayment(@Valid @RequestBody PaymentRequestDTO request) {
        log.info("Received payment request: {}", request);
        PaymentResponseDTO response = paymentService.processPayment(request);

        return ResponseEntity.ok(response);
    }


}
