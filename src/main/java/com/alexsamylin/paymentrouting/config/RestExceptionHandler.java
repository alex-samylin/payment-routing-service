package com.alexsamylin.paymentrouting.config;

import com.alexsamylin.paymentrouting.dto.PaymentResponseDTO;
import com.alexsamylin.paymentrouting.error.ProviderBusinessException;
import com.alexsamylin.paymentrouting.error.ProviderTechnicalException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(ProviderBusinessException.class)
    public ResponseEntity<PaymentResponseDTO> handleBusinessError(ProviderBusinessException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(PaymentResponseDTO.builder()
                        .provider(ex.getProvider())
                        .transactionId(ex.getTransactionId())
                        .message(Optional.ofNullable(ex.getErrorCode()).map(Enum::toString).orElse(null))
                        .build());
    }

    @ExceptionHandler(ProviderTechnicalException.class)
    public ResponseEntity<PaymentResponseDTO> handleTechnicalError(ProviderTechnicalException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(PaymentResponseDTO.builder()
                        .provider(ex.getProvider())
                        .transactionId(ex.getTransactionId())
                        .message(ex.getMessage())
                        .build());
    }

}
