package com.nivora.pay.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nivora.pay.entities.Transaction;
import com.nivora.pay.entities.TransactionStatus;
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
    public Transaction createTransaction(Long fromWalletId,
            Long toWalletId,
            BigDecimal amount,
            String description) {

        log.info("Creating transaction from {} to {} of amount {}",
                fromWalletId, toWalletId, amount);

        Transaction transaction = Transaction.builder()
                .fromWalletId(fromWalletId)
                .toWalletId(toWalletId)
                .amount(amount)
                .description(description)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction created with id: {}", savedTransaction.getId());

        return savedTransaction;

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
}
