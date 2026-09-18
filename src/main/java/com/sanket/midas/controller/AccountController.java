package com.sanket.midas.controller;

import com.sanket.midas.dto.CreateAccountRequest;
import com.sanket.midas.dto.MoneyRequest;
import com.sanket.midas.dto.TransferRequest;
import com.sanket.midas.entity.Account;
import com.sanket.midas.entity.BankTransaction;
import com.sanket.midas.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // Create an account
    @PostMapping
    public ResponseEntity<Account> createAccount(
            @RequestBody @Valid CreateAccountRequest request) {

        Account account = accountService.createAccount(
                request.getName(),
                request.getEmail(),
                request.getBalance()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(account);
    }

    // Get one account
    @GetMapping("/{id}")
    public Account getAccount(@PathVariable Long id) {

        return accountService.getAccount(id);
    }

    // Get all accounts
    @GetMapping
    public List<Account> getAllAccounts() {

        return accountService.getAllAccounts();
    }

    // Deposit money
    @PostMapping("/{id}/deposit")
    public Account deposit(
            @PathVariable Long id,
            @RequestBody @Valid MoneyRequest request) {

        return accountService.deposit(
                id,
                request.getAmount()
        );
    }

    // Withdraw money
    @PostMapping("/{id}/withdraw")
    public Account withdraw(
            @PathVariable Long id,
            @RequestBody @Valid MoneyRequest request) {

        return accountService.withdraw(
                id,
                request.getAmount()
        );
    }

    // Transfer money between accounts
    @PostMapping("/transfer")
    public BankTransaction transfer(
            @RequestBody @Valid TransferRequest request) {

        return accountService.transfer(
                request.getSenderAccountId(),
                request.getRecipientAccountId(),
                request.getAmount()
        );
    }

    // Get transaction history
    @GetMapping("/{id}/transactions")
    public List<BankTransaction> getTransactionHistory(
            @PathVariable Long id) {

        return accountService.getTransactionHistory(id);
    }
}