package com.sanket.midas.controller;

import com.sanket.midas.entity.Reward;
import com.sanket.midas.service.AccountService;
import com.sanket.midas.repository.RewardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RewardControllerTest {

    private MockMvc mockMvc;

    private RewardRepository rewardRepository;

    private AccountService accountService;

    @BeforeEach
    void setUp() {

        rewardRepository =
                Mockito.mock(RewardRepository.class);

        accountService =
                Mockito.mock(AccountService.class);

        RewardController rewardController =
                new RewardController(
                        rewardRepository,
                        accountService
                );

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(rewardController)
                        .build();
    }

    @Test
    void shouldReturnRewardsForAccount()
            throws Exception {

        Reward reward =
                new Reward(
                        7L,
                        1L,
                        new BigDecimal("500.00"),
                        new BigDecimal("5.00"),
                        "1% transfer reward, maximum ₹100",
                        LocalDateTime.now()
                );

        when(rewardRepository
                .findByAccountIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(reward));

        mockMvc.perform(
                get("/api/rewards/account/1")
        )
        .andExpect(status().isOk())
        .andExpect(
                jsonPath("$.length()")
                        .value(1)
        )
        .andExpect(
                jsonPath("$[0].transactionId")
                        .value(7)
        )
        .andExpect(
                jsonPath("$[0].accountId")
                        .value(1)
        )
        .andExpect(
                jsonPath("$[0].transactionAmount")
                        .value(500.00)
        )
        .andExpect(
                jsonPath("$[0].rewardAmount")
                        .value(5.00)
        );

        verify(accountService)
                .getAccount(1L);
    }

    @Test
    void shouldReturnEmptyListWhenAccountHasNoRewards()
            throws Exception {

        when(rewardRepository
                .findByAccountIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        mockMvc.perform(
                get("/api/rewards/account/1")
        )
        .andExpect(status().isOk())
        .andExpect(
                jsonPath("$.length()")
                        .value(0)
        );

        verify(accountService)
                .getAccount(1L);
    }

    @Test
    void shouldReturnRewardForTransaction()
            throws Exception {

        Reward reward =
                new Reward(
                        7L,
                        1L,
                        new BigDecimal("500.00"),
                        new BigDecimal("5.00"),
                        "1% transfer reward, maximum ₹100",
                        LocalDateTime.now()
                );

        when(rewardRepository
                .findByTransactionId(7L))
                .thenReturn(Optional.of(reward));

        mockMvc.perform(
                get("/api/rewards/transaction/7")
        )
        .andExpect(status().isOk())
        .andExpect(
                jsonPath("$.transactionId")
                        .value(7)
        )
        .andExpect(
                jsonPath("$.rewardAmount")
                        .value(5.00)
        );
    }

    @Test
    void shouldReturnNotFoundWhenRewardDoesNotExist()
            throws Exception {

        when(rewardRepository
                .findByTransactionId(999L))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                get("/api/rewards/transaction/999")
        )
        .andExpect(status().isNotFound());
    }
}