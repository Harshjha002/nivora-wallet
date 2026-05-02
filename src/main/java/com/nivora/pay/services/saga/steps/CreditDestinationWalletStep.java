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
public class CreditDestinationWalletStep implements SagaStepInterface {

    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public boolean execute(SagaContext context) {

        Long toWalletId = context.getLong("toWalletId");
        BigDecimal amount = context.getBigDecimal("amount");

        log.info("Crediting destination wallet {} with amount {}", toWalletId, amount);

        Wallet wallet = walletRepository.findByUserIdWithLock(toWalletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        // before state
        context.put("destinationWalletBalanceBeforeCredit", wallet.getBalance());

        BigDecimal newBalance = wallet.getBalance().add(amount);

        walletRepository.updateBalanceByUserId(
                wallet.getUserId(),
                newBalance);

        log.info("Wallet {} credited. New balance {}", toWalletId, wallet.getBalance());

        // after state
        context.put("destinationWalletBalanceAfterCredit", wallet.getBalance());

        return true;
    }

    @Override
    @Transactional
    public boolean compensate(SagaContext context) {

        Long toWalletId = context.getLong("toWalletId");
        BigDecimal amount = context.getBigDecimal("amount");

        log.info("Compensating credit for wallet {} with amount {}", toWalletId, amount);

        Wallet wallet = walletRepository.findByUserIdWithLock(toWalletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        context.put("destinationWalletBalanceBeforeCompensation", wallet.getBalance());

        BigDecimal newBalance = wallet.getBalance().subtract(amount);

        walletRepository.updateBalanceByUserId(
                wallet.getUserId(),
                newBalance);

        log.info("Wallet {} compensated. New balance {}", toWalletId, wallet.getBalance());

        context.put("destinationWalletBalanceAfterCompensation", wallet.getBalance());

        return true;
    }

    @Override
    public String getStepName() {
        return SagaStepType.CREDIT_DESTINATION_WALLET_STEP.toString();
    }
}