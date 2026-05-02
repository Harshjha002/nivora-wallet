package com.nivora.pay.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.nivora.pay.entities.Transaction;
import com.nivora.pay.entities.TransactionStatus;
import com.nivora.pay.entities.TransactionType;
import com.nivora.pay.repositories.TransactionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Transactional
    public Transaction createTransaction(
            Long fromWalletId,
            Long toWalletId,
            BigDecimal amount,
            String description,
            String idempotencyKey

    ) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key is required");
        }

        Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(idempotencyKey);

        if (existing.isPresent()) {

            Transaction tx = existing.get();

            log.info("Idempotent hit for key {} with status {}", idempotencyKey, tx.getStatus());

            // already processed → safe
            if (tx.getStatus() == TransactionStatus.SUCCESS) {
                return tx;
            }

            // still processing → return but DO NOT re-run saga
            if (tx.getStatus() == TransactionStatus.PENDING) {
                return tx;
            }

            // failed → still return same result (strict idempotency)
            if (tx.getStatus() == TransactionStatus.FAILED) {
                return tx;
            }

            return tx;
        }
        log.info("Creating transaction from wallet {} to {} amount {} with key {}",
                fromWalletId, toWalletId, amount, idempotencyKey);

        Transaction transaction = Transaction.builder()
                .fromWalletId(fromWalletId)
                .toWalletId(toWalletId)
                .amount(amount)
                .status(TransactionStatus.PENDING)
                .type(TransactionType.TRANSFER)
                .description(description)
                .idempotencyKey(idempotencyKey)
                .build();

        try {
            return transactionRepository.save(transaction);
        } catch (DataIntegrityViolationException e) {
            return transactionRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> e);
        }

    }

    // Get single transaction
    public Transaction getTransactionById(Long id) {

        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction id not found: " + id));
    }

    // Get all transactions for a wallet (incoming + outgoing)
    public List<Transaction> getTransactionsByWalletId(Long walletId) {
        return transactionRepository.findByWalletId(walletId);
    }

    // Get sent transactions
    public List<Transaction> getSentTransactions(Long walletId) {
        return transactionRepository.findByFromWalletId(walletId);
    }

    // Get received transactions
    public List<Transaction> getReceivedTransactions(Long walletId) {
        return transactionRepository.findByToWalletId(walletId);
    }

    // Filter by status
    public List<Transaction> getTransactionsByStatus(TransactionStatus status) {
        return transactionRepository.findByStatus(status);
    }

    // Saga transactions (for distributed systems)
    public List<Transaction> getTransactionsBySagaId(Long sagaId) {
        return transactionRepository.findBySagaInstanceId(sagaId);
    }

    // Save transaction
    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    // Delete transaction
    public void deleteTransaction(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new RuntimeException("Transaction not found with id: " + id);
        }
        transactionRepository.deleteById(id);
    }

    public void updateTransactionWithSagaInstanceId(Long transactionId, Long sagaInstanceId) {
        Transaction transaction = getTransactionById(transactionId);
        transaction.setSagaInstanceId(sagaInstanceId);
        transactionRepository.save(transaction);
        log.info("Transaction updated with saga instance id {}", sagaInstanceId);
    }
}
