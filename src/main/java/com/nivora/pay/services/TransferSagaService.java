package com.nivora.pay.services;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.nivora.pay.entities.Transaction;
import com.nivora.pay.services.saga.SagaContext;
import com.nivora.pay.services.saga.SagaOrchestrator;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransferSagaService {

    private final TransactionService transactionService;
    private final SagaOrchestrator sagaOrchestrator;
    private final TransferSagaAsyncService asyncService;

    @Transactional
    public Long initiateTransfer(Long fromWalletId,
            Long toWalletId,
            BigDecimal amount,
            String description,
            String idempotencyKey) {

        // Validate idempotency key to prevent duplicate processing
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key is required");
        }

        // Validate transfer amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        // Create or fetch existing transaction (idempotent)
        Transaction transaction = transactionService.createTransaction(
                fromWalletId,
                toWalletId,
                amount,
                description,
                idempotencyKey);

        // If saga already exists → return existing (avoid duplicate execution)
        if (transaction.getSagaInstanceId() != null) {
            log.info("Duplicate request for key {} → returning saga {}",
                    idempotencyKey, transaction.getSagaInstanceId());
            return transaction.getSagaInstanceId();
        }

        // Start new saga for transaction processing
        log.info("Starting saga for transaction {} with key {}",
                transaction.getId(), idempotencyKey);

        // Build saga context (shared data across steps)
        SagaContext sagaContext = SagaContext.builder()
                .data(Map.of(
                        "transactionId", transaction.getId(),
                        "fromWalletId", fromWalletId,
                        "toWalletId", toWalletId,
                        "amount", amount,
                        "description", description))
                .build();

        // Initialize saga instance
        Long sagaInstanceId = sagaOrchestrator.startSaga(sagaContext);

        // Link transaction with saga instance
        transactionService.updateTransactionWithSagaInstanceId(
                transaction.getId(),
                sagaInstanceId);

        // Execute saga asynchronously
        asyncService.executeTransferSaga(sagaInstanceId);

        return sagaInstanceId;
    }
}