package com.sanket.midas.entity;

import com.sanket.midas.transaction.TransactionStatus;
import com.sanket.midas.transaction.TransactionType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_transaction")
public class BankTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(nullable = false)
    private Long accountId;

    private Long recipientAccountId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private String description;

    public BankTransaction() {
    }

    public BankTransaction(
            TransactionType type,
            BigDecimal amount,
            TransactionStatus status,
            Long accountId,
            Long recipientAccountId,
            LocalDateTime createdAt,
            String description) {

        this.type = type;
        this.amount = amount;
        this.status = status;
        this.accountId = accountId;
        this.recipientAccountId = recipientAccountId;
        this.createdAt = createdAt;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public Long getAccountId() {
        return accountId;
    }

    public Long getRecipientAccountId() {
        return recipientAccountId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
}