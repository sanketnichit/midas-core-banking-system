package com.sanket.midas.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class TransferRequest {

    @NotNull(message = "Sender account ID is required")
    private Long senderAccountId;

    @NotNull(message = "Recipient account ID is required")
    private Long recipientAccountId;

    @NotNull(message = "Amount is required")
    @DecimalMin(
            value = "0.01",
            message = "Transfer amount must be greater than 0"
    )
    private BigDecimal amount;

    public TransferRequest() {
    }

    public Long getSenderAccountId() {
        return senderAccountId;
    }

    public void setSenderAccountId(Long senderAccountId) {
        this.senderAccountId = senderAccountId;
    }

    public Long getRecipientAccountId() {
        return recipientAccountId;
    }

    public void setRecipientAccountId(Long recipientAccountId) {
        this.recipientAccountId = recipientAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}