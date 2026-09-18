package com.sanket.midas.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "reward",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reward_transaction_id",
                        columnNames = "transaction_id"
                )
        }
)
public class Reward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(nullable = false)
    private BigDecimal transactionAmount;

    @Column(nullable = false)
    private BigDecimal rewardAmount;

    @Column(nullable = false)
    private String rule;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Reward() {
    }

    public Reward(
            Long transactionId,
            Long accountId,
            BigDecimal transactionAmount,
            BigDecimal rewardAmount,
            String rule,
            LocalDateTime createdAt) {

        this.transactionId = transactionId;
        this.accountId = accountId;
        this.transactionAmount = transactionAmount;
        this.rewardAmount = rewardAmount;
        this.rule = rule;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public BigDecimal getRewardAmount() {
        return rewardAmount;
    }

    public String getRule() {
        return rule;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}