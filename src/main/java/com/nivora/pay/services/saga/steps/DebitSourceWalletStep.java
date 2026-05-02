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

        Wallet wallet = walletRepository.findByUserIdWithLock(fromWalletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (!wallet.hasSufficientBalance(amount)) {
            log.error("Insufficient balance for wallet {}", fromWalletId);
            return false;
        }

        // before state
        context.put("sourceWalletBalanceBeforeDebit", wallet.getBalance());

        BigDecimal newBalance = wallet.getBalance().subtract(amount);

        walletRepository.updateBalanceByUserId(
                wallet.getUserId(),
                newBalance);
        log.info("Wallet {} debited. New balance {}", fromWalletId, wallet.getBalance());

        // after state
        context.put("sourceWalletBalanceAfterDebit", wallet.getBalance());

        return true;
    }

    @Override
    @Transactional
    public boolean compensate(SagaContext context) {

        Long fromWalletId = context.getLong("fromWalletId");
        BigDecimal amount = context.getBigDecimal("amount");

        log.info("Compensating debit for wallet {} with amount {}", fromWalletId, amount);

        Wallet wallet = walletRepository.findByUserIdWithLock(fromWalletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        context.put("sourceWalletBalanceBeforeCompensation", wallet.getBalance());

        BigDecimal newBalance = wallet.getBalance().add(amount);

        walletRepository.updateBalanceByUserId(
                wallet.getUserId(),
                newBalance);

        log.info("Wallet {} compensated. New balance {}", fromWalletId, wallet.getBalance());

        context.put("sourceWalletBalanceAfterCompensation", wallet.getBalance());

        return true;
    }

    @Override
    public String getStepName() {
        return SagaStepType.DEBIT_SOURCE_WALLET_STEP.toString();
    }
}