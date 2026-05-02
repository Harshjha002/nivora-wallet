package com.nivora.pay.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nivora.pay.dtos.TransferRequestDTO;
import com.nivora.pay.dtos.TransferResponseDTO;
import com.nivora.pay.services.TransactionService;
import com.nivora.pay.services.TransferSagaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transactions")
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;
    private final TransferSagaService transferSagaService;

    @PostMapping
    public ResponseEntity<TransferResponseDTO> createTransaction(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody TransferRequestDTO request) {

        try {
            Long sagaInstanceId = transferSagaService.initiateTransfer(
                    request.getFromWalletId(),
                    request.getToWalletId(),
                    request.getAmount(),
                    request.getDescription(),
                    idempotencyKey);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            TransferResponseDTO.builder()
                                    .sagaInstanceId(sagaInstanceId)
                                    .build());
        } catch (Exception e) {
            log.error("Error Creating transaction", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
