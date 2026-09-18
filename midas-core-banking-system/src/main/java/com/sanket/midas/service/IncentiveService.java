package com.sanket.midas.service;

import com.sanket.midas.entity.Account;
import com.sanket.midas.entity.Reward;
import com.sanket.midas.kafka.BankingEvent;
import com.sanket.midas.repository.AccountRepository;
import com.sanket.midas.repository.RewardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class IncentiveService {

    private static final BigDecimal REWARD_RATE =
            new BigDecimal("0.01");

    private static final BigDecimal MAX_REWARD =
            new BigDecimal("100.00");

    private final RewardRepository rewardRepository;
    private final AccountRepository accountRepository;

    public IncentiveService(
            RewardRepository rewardRepository,
            AccountRepository accountRepository) {

        this.rewardRepository = rewardRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void processEvent(BankingEvent event) {

        if (!"TRANSFER".equals(event.type())) {
            return;
        }

        if (!"COMPLETED".equals(event.status())) {
            return;
        }

        if (rewardRepository
                .findByTransactionId(event.transactionId())
                .isPresent()) {

            System.out.println(
                    "Reward already exists for transaction: "
                            + event.transactionId()
            );

            return;
        }

        BigDecimal rewardAmount =
                event.amount()
                        .multiply(REWARD_RATE)
                        .setScale(2, RoundingMode.HALF_UP);

        if (rewardAmount.compareTo(MAX_REWARD) > 0) {
            rewardAmount = MAX_REWARD;
        }

        Account account =
                accountRepository.findById(event.accountId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Account with ID "
                                                + event.accountId()
                                                + " not found"
                                )
                        );

        account.setBalance(
                account.getBalance().add(rewardAmount)
        );

        accountRepository.save(account);

        Reward reward =
                new Reward(
                        event.transactionId(),
                        event.accountId(),
                        event.amount(),
                        rewardAmount,
                        "1% transfer reward, maximum ₹100",
                        LocalDateTime.now()
                );

        rewardRepository.save(reward);

        System.out.println(
                "Reward created and credited: ₹"
                        + rewardAmount
                        + " for transaction "
                        + event.transactionId()
        );
    }
}