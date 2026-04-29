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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;



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
    public ResponseEntity<Wallet> getMethodName(@PathVariable Long id) {
        Wallet wallet = walletService.getWalletById(id);
            return ResponseEntity.ok(wallet);
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getWalletBalance(@PathVariable Long id) {
        BigDecimal balance = walletService.getWalletBalance(id);
        return ResponseEntity.ok(balance);
    }

    @PostMapping("/{id}/debit")
    public ResponseEntity<Wallet> debitWallet(@PathVariable Long id , @RequestBody DebitWalletRequestDTO request) {
       walletService.debit(id,request.getAmount());
        Wallet wallet = walletService.getWalletById(id);
        return ResponseEntity.ok(wallet);
    }


    @PostMapping("/{id}/credit")
    public ResponseEntity<Wallet> creditWallet(@PathVariable Long id , @RequestBody CreditWalletRequestDTO request) {
        walletService.credit(id,request.getAmount());
        Wallet wallet = walletService.getWalletById(id);
        return ResponseEntity.ok(wallet);
    }

    

    
    
    
    
}
