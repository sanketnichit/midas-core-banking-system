package com.sanket.midas.controller;

import com.sanket.midas.entity.Reward;
import com.sanket.midas.service.AccountService;
import com.sanket.midas.repository.RewardRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rewards")
public class RewardController {

    private final RewardRepository rewardRepository;
    private final AccountService accountService;

    public RewardController(
            RewardRepository rewardRepository,
            AccountService accountService) {

        this.rewardRepository = rewardRepository;
        this.accountService = accountService;
    }

    @GetMapping("/account/{accountId}")
    public List<Reward> getRewardsByAccount(
            @PathVariable Long accountId) {

        // Make sure the account actually exists.
        accountService.getAccount(accountId);

        return rewardRepository
                .findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<Reward> getRewardByTransaction(
            @PathVariable Long transactionId) {

        return rewardRepository
                .findByTransactionId(transactionId)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }
}