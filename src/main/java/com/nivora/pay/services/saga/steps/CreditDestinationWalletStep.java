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
public class CreditDestinationWalletStep implements SagaStep {

    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public boolean execute(SagaContext context) {

        Long toWalletId = context.getLong("toWalletId");
        BigDecimal amount = context.getBigDecimal("amount");

        log.info("Crediting destination wallet {} with amount {}", toWalletId, amount);

        Wallet wallet = walletRepository.findByIdWithLock(toWalletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        log.info("Wallet fetched with balance {}", wallet.getBalance());

        // Store balance before credit
        context.put("destinationWalletBalanceBeforeCredit", wallet.getBalance());

        // Perform credit
        wallet.credit(amount);
        walletRepository.save(wallet);

        log.info("Wallet {} credited. New balance {}", toWalletId, wallet.getBalance());

        // Store balance after credit
        context.put("destinationWalletBalanceAfterCredit", wallet.getBalance());

        log.info("Credit destination wallet step executed successfully");
        return true;
    }

    @Override
    @Transactional
    public boolean compensate(SagaContext context) {

        Long toWalletId = context.getLong("toWalletId");
        BigDecimal amount = context.getBigDecimal("amount");

        log.info("Compensating (reversing credit) for destination wallet {} with amount {}", toWalletId, amount);

        Wallet wallet = walletRepository.findByIdWithLock(toWalletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        log.info("Wallet fetched with balance {}", wallet.getBalance());

        // Store balance before compensation
        context.put("destinationWalletBalanceBeforeCompensation", wallet.getBalance());

        // Reverse credit (debit)
        wallet.debit(amount);
        walletRepository.save(wallet);

        log.info("Wallet {} compensated. New balance {}", toWalletId, wallet.getBalance());

        // Store balance after compensation
        context.put("destinationWalletBalanceAfterCompensation", wallet.getBalance());

        log.info("Credit destination wallet compensation executed successfully");
        return true;
    }

    @Override
    public String getStepName() {
        return "CreditDestinationWalletStep";
    }
}