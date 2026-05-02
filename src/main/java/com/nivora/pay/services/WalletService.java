package com.nivora.pay.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nivora.pay.entities.Wallet;
import com.nivora.pay.repositories.WalletRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    public Wallet createWallet(Long userId) {
        log.info("Creating wallet for user {}", userId);

        Wallet wallet = Wallet.builder()
                .userId(userId)
                .isActive(true)
                .balance(BigDecimal.ZERO)
                .build();

        wallet = walletRepository.save(wallet);

        return wallet;
    }

    public Wallet getWalletById(Long walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found with id: " + walletId));

    }

    public List<Wallet> getWalletsByUserId(Long userId) {
        return walletRepository.findByUserId(userId);
    }

    public Wallet getWalletByUserId(Long userid) {
        log.info("Getting wallet by user id {}", userid);
        return walletRepository.findByUserId(userid).get(0);
    }

    @Transactional
    public void debit(Long userId, BigDecimal amount) {

        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (!wallet.hasSufficientBalance(amount)) {
            throw new RuntimeException("Insufficient balance");
        }

        BigDecimal newBalance = wallet.getBalance().subtract(amount);

        walletRepository.updateBalanceByUserId(userId, newBalance);
    }

    @Transactional
    public void credit(Long userId, BigDecimal amount) {

        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        BigDecimal newBalance = wallet.getBalance().add(amount);

        walletRepository.updateBalanceByUserId(userId, newBalance);
    }

    public BigDecimal getWalletBalance(long walletId) {
        log.info("Getting balance for wallet {}", walletId);
        BigDecimal balance = getWalletById(walletId).getBalance();
        log.info("Balance for wallet {} is {}", walletId, balance);
        return balance;
    }

}
