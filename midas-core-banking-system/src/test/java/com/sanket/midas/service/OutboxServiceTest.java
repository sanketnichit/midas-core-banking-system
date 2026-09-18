package com.sanket.midas.service;

import com.sanket.midas.entity.OutboxEvent;
import com.sanket.midas.kafka.BankingEvent;
import com.sanket.midas.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxServiceTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private JsonMapper jsonMapper;

    private OutboxService outboxService;

    @BeforeEach
    void setUp() {
        outboxService =
                new OutboxService(
                        outboxEventRepository,
                        jsonMapper
                );
    }

    @Test
    void shouldSaveBankingEventToOutbox() throws Exception {

        LocalDateTime timestamp =
                LocalDateTime.of(
                        2026,
                        9,
                        18,
                        19,
                        30
                );

        BankingEvent event =
                new BankingEvent(
                        200L,
                        "TRANSFER",
                        1L,
                        2L,
                        new BigDecimal("500.00"),
                        "COMPLETED",
                        timestamp,
                        "Transfer between accounts"
                );

        String expectedPayload =
                "{\"transactionId\":200,\"type\":\"TRANSFER\"}";

        when(jsonMapper.writeValueAsString(event))
                .thenReturn(expectedPayload);

        when(outboxEventRepository.save(any(OutboxEvent.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        OutboxEvent savedEvent =
                outboxService.saveEvent(event);

        assertNotNull(savedEvent);

        ArgumentCaptor<OutboxEvent> captor =
                ArgumentCaptor.forClass(OutboxEvent.class);

        verify(outboxEventRepository)
                .save(captor.capture());

        OutboxEvent capturedEvent =
                captor.getValue();

        assertEquals(
                200L,
                capturedEvent.getTransactionId()
        );

        assertEquals(
                "TRANSFER",
                capturedEvent.getEventType()
        );

        assertEquals(
                "1",
                capturedEvent.getAggregateId()
        );

        assertEquals(
                expectedPayload,
                capturedEvent.getPayload()
        );

        assertNotNull(
                capturedEvent.getCreatedAt()
        );

        assertNull(
                capturedEvent.getPublishedAt()
        );

        verify(jsonMapper)
                .writeValueAsString(event);
    }

    @Test
    void shouldCreateOutboxEventForDeposit() throws Exception {

        BankingEvent event =
                new BankingEvent(
                        201L,
                        "DEPOSIT",
                        1L,
                        null,
                        new BigDecimal("1000.00"),
                        "COMPLETED",
                        LocalDateTime.now(),
                        "Cash deposit"
                );

        String payload =
                "{\"transactionId\":201,\"type\":\"DEPOSIT\"}";

        when(jsonMapper.writeValueAsString(event))
                .thenReturn(payload);

        when(outboxEventRepository.save(any(OutboxEvent.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        OutboxEvent savedEvent =
                outboxService.saveEvent(event);

        assertEquals(
                201L,
                savedEvent.getTransactionId()
        );

        assertEquals(
                "DEPOSIT",
                savedEvent.getEventType()
        );

        assertEquals(
                "1",
                savedEvent.getAggregateId()
        );

        assertEquals(
                payload,
                savedEvent.getPayload()
        );

        verify(outboxEventRepository)
                .save(any(OutboxEvent.class));
    }
}