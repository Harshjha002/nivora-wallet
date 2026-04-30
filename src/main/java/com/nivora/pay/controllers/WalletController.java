package com.nivora.pay.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nivora.pay.dtos.CreateWalletRequestDTO;
import com.nivora.pay.dtos.CreditWalletRequestDTO;
import com.nivora.pay.dtos.DebitWalletRequestDTO;
import com.nivora.pay.entities.Wallet;
import com.nivora.pay.services.WalletService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wallet")
@Slf4j
public class WalletController {
    private final WalletService walletService;

    @PostMapping()
    public ResponseEntity<Wallet> createWallet(@RequestBody CreateWalletRequestDTO request) {
        try {
            Wallet newWallet = walletService.createWallet(request.getUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body(newWallet);
        } catch (Exception e) {
            log.error("Error creating wallet" , e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<Wallet> getByWalletId(@PathVariable Long id) {
        Wallet wallet = walletService.getWalletById(id);
            return ResponseEntity.ok(wallet);
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getWalletBalance(@PathVariable Long id) {
        BigDecimal balance = walletService.getWalletBalance(id);
        return ResponseEntity.ok(balance);
    }

    @PostMapping("/{userid}/debit")
    public ResponseEntity<Wallet> debitWallet(@PathVariable Long userid , @RequestBody DebitWalletRequestDTO request) {
       walletService.debit(userid,request.getAmount());
        Wallet wallet = walletService.getWalletByUserId(userid);
        return ResponseEntity.ok(wallet);
    }


    @PostMapping("/{userid}/credit")
    public ResponseEntity<Wallet> creditWallet(@PathVariable Long userid , @RequestBody CreditWalletRequestDTO request) {
        walletService.credit(userid,request.getAmount());
        Wallet wallet = walletService.getWalletByUserId(userid);
        return ResponseEntity.ok(wallet);
    }

    

    
    
    
    
}
