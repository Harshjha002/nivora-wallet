package com.nivora.pay.services.saga.steps;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.nivora.pay.entities.Wallet;
import com.nivora.pay.repositories.WalletRepository;
import com.nivora.pay.services.saga.SagaContext;
import com.nivora.pay.services.saga.SagaStepInterface;
import com.nivora.pay.services.saga.steps.SagaStepFactory.SagaStepType;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DebitSourceWalletStep implements SagaStepInterface {

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
       walletRepository.updateBalanceByUserId(fromWalletId, wallet.getBalance().subtract(amount));

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
        walletRepository.updateBalanceByUserId(fromWalletId, wallet.getBalance().add(amount));


        log.info("Wallet {} compensated. New balance {}", fromWalletId, wallet.getBalance());

        // Store balance after compensation
        context.put("sourceWalletBalanceAfterCompensation", wallet.getBalance());

        log.info("Compensation for source wallet step executed successfully");
        return true;
    }

    @Override
    public String getStepName() {
        return SagaStepType.DEBIT_SOURCE_WALLET_STEP.toString();
    }
}