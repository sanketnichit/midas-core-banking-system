package com.sanket.midas.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "outbox_event",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_outbox_transaction_id",
                        columnNames = "transaction_id"
                )
        }
)
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false, unique = true)
    private Long transactionId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String payload;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime publishedAt;

    public OutboxEvent() {
    }

    public OutboxEvent(
            Long transactionId,
            String eventType,
            String aggregateId,
            String payload,
            LocalDateTime createdAt) {

        this.transactionId = transactionId;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getPayload() {
        return payload;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void markPublished() {
        this.publishedAt = LocalDateTime.now();
    }
}