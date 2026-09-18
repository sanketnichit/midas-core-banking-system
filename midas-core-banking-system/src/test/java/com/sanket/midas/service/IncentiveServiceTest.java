package com.sanket.midas.service;

import com.sanket.midas.entity.Account;
import com.sanket.midas.entity.Reward;
import com.sanket.midas.kafka.BankingEvent;
import com.sanket.midas.repository.AccountRepository;
import com.sanket.midas.repository.RewardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncentiveServiceTest {

    @Mock
    private RewardRepository rewardRepository;

    @Mock
    private AccountRepository accountRepository;

    private IncentiveService incentiveService;

    @BeforeEach
    void setUp() {
        incentiveService =
                new IncentiveService(
                        rewardRepository,
                        accountRepository
                );
    }

    @Test
    void shouldCreateOnePercentRewardForCompletedTransfer() {

        Account account =
                new Account(
                        "Sanket",
                        "sanket@example.com",
                        new BigDecimal("1000.00")
                );

        BankingEvent event =
                new BankingEvent(
                        101L,
                        "TRANSFER",
                        1L,
                        2L,
                        new BigDecimal("500.00"),
                        "COMPLETED",
                        LocalDateTime.now(),
                        "Transfer between accounts"
                );

        when(rewardRepository.findByTransactionId(101L))
                .thenReturn(Optional.empty());

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        incentiveService.processEvent(event);

        assertEquals(
                new BigDecimal("1005.00"),
                account.getBalance()
        );

        ArgumentCaptor<Reward> rewardCaptor =
                ArgumentCaptor.forClass(Reward.class);

        verify(rewardRepository)
                .save(rewardCaptor.capture());

        Reward savedReward =
                rewardCaptor.getValue();

        assertEquals(
                101L,
                savedReward.getTransactionId()
        );

        assertEquals(
                1L,
                savedReward.getAccountId()
        );

        assertEquals(
                new BigDecimal("500.00"),
                savedReward.getTransactionAmount()
        );

        assertEquals(
                new BigDecimal("5.00"),
                savedReward.getRewardAmount()
        );

        verify(accountRepository)
                .save(account);
    }

    @Test
    void shouldNotCreateRewardForDeposit() {

        BankingEvent event =
                new BankingEvent(
                        102L,
                        "DEPOSIT",
                        1L,
                        null,
                        new BigDecimal("500.00"),
                        "COMPLETED",
                        LocalDateTime.now(),
                        "Cash deposit"
                );

        incentiveService.processEvent(event);

        verifyNoInteractions(
                rewardRepository,
                accountRepository
        );
    }

    @Test
    void shouldNotCreateRewardForIncompleteTransfer() {

        BankingEvent event =
                new BankingEvent(
                        103L,
                        "TRANSFER",
                        1L,
                        2L,
                        new BigDecimal("500.00"),
                        "FAILED",
                        LocalDateTime.now(),
                        "Transfer between accounts"
                );

        incentiveService.processEvent(event);

        verifyNoInteractions(
                rewardRepository,
                accountRepository
        );
    }

    @Test
    void shouldNotCreateDuplicateReward() {

        Reward existingReward =
                new Reward(
                        104L,
                        1L,
                        new BigDecimal("500.00"),
                        new BigDecimal("5.00"),
                        "1% transfer reward, maximum ₹100",
                        LocalDateTime.now()
                );

        BankingEvent event =
                new BankingEvent(
                        104L,
                        "TRANSFER",
                        1L,
                        2L,
                        new BigDecimal("500.00"),
                        "COMPLETED",
                        LocalDateTime.now(),
                        "Transfer between accounts"
                );

        when(rewardRepository.findByTransactionId(104L))
                .thenReturn(Optional.of(existingReward));

        incentiveService.processEvent(event);

        verify(rewardRepository, never())
                .save(any(Reward.class));

        verify(accountRepository, never())
                .findById(anyLong());

        verify(accountRepository, never())
                .save(any(Account.class));
    }

    @Test
    void shouldCapRewardAtOneHundredRupees() {

        Account account =
                new Account(
                        "Sanket",
                        "sanket@example.com",
                        new BigDecimal("1000.00")
                );

        BankingEvent event =
                new BankingEvent(
                        105L,
                        "TRANSFER",
                        1L,
                        2L,
                        new BigDecimal("20000.00"),
                        "COMPLETED",
                        LocalDateTime.now(),
                        "Large transfer"
                );

        when(rewardRepository.findByTransactionId(105L))
                .thenReturn(Optional.empty());

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        incentiveService.processEvent(event);

        assertEquals(
                new BigDecimal("1100.00"),
                account.getBalance()
        );

        ArgumentCaptor<Reward> rewardCaptor =
                ArgumentCaptor.forClass(Reward.class);

        verify(rewardRepository)
                .save(rewardCaptor.capture());

        assertEquals(
                new BigDecimal("100.00"),
                rewardCaptor.getValue().getRewardAmount()
        );
    }
}