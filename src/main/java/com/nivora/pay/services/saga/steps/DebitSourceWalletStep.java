package com.nivora.pay.services.saga.steps;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.nivora.pay.entities.Wallet;
import com.nivora.pay.repositories.WalletRepository;
import com.nivora.pay.services.saga.SagaContext;
import com.nivora.pay.services.saga.SagaStep;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DebitSourceWalletStep implements SagaStep {

    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public boolean execute(SagaContext context) {
        Long fromWalletId = context.getLong("fromWalletId");
        BigDecimal amount = context.getBigDecimal("amount");

        log.info("Debiting source wallet {} with amount {}", fromWalletId, amount);

        Wallet wallet = walletRepository.findByIdWithLock(fromWalletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        log.info("Wallet fetched with balance {}", wallet.getBalance());

        // Store balance before debit
        context.put("sourceWalletBalanceBeforeDebit", wallet.getBalance());

        // Perform debit
        wallet.debit(amount);
        walletRepository.save(wallet);

        log.info("Wallet {} debited. New balance {}", fromWalletId, wallet.getBalance());

        // Store balance after debit
        context.put("sourceWalletBalanceAfterDebit", wallet.getBalance());

        log.info("Debit source wallet step executed successfully");
        return true;
    }

    @Override
    @Transactional
    public boolean compensate(SagaContext context) {
        Long fromWalletId = context.getLong("fromWalletId");
        BigDecimal amount = context.getBigDecimal("amount");

        log.info("Compensating (reversing debit) for source wallet {} with amount {}", fromWalletId, amount);

        Wallet wallet = walletRepository.findByIdWithLock(fromWalletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        log.info("Wallet fetched with balance {}", wallet.getBalance());

        // Store balance before compensation
        context.put("sourceWalletBalanceBeforeCompensation", wallet.getBalance());

        // Reverse debit (credit back)
        wallet.credit(amount);
        walletRepository.save(wallet);

        log.info("Wallet {} compensated. New balance {}", fromWalletId, wallet.getBalance());

        // Store balance after compensation
        context.put("sourceWalletBalanceAfterCompensation", wallet.getBalance());

        log.info("Compensation for source wallet step executed successfully");
        return true;
    }

    @Override
    public String getStepName() {
        return "DebitSourceWalletStep";
    }
}