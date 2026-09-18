package com.sanket.midas.service;

import com.sanket.midas.entity.OutboxEvent;
import com.sanket.midas.kafka.BankingEvent;
import com.sanket.midas.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;

@Service
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final JsonMapper jsonMapper;

    public OutboxService(
            OutboxEventRepository outboxEventRepository,
            JsonMapper jsonMapper) {

        this.outboxEventRepository = outboxEventRepository;
        this.jsonMapper = jsonMapper;
    }

    public OutboxEvent saveEvent(BankingEvent event) {

        try {
            String payload =
                    jsonMapper.writeValueAsString(event);

            OutboxEvent outboxEvent =
                    new OutboxEvent(
                            event.transactionId(),
                            event.type(),
                            String.valueOf(event.accountId()),
                            payload,
                            LocalDateTime.now()
                    );

            return outboxEventRepository.save(outboxEvent);

        } catch (JacksonException exception) {

            throw new IllegalStateException(
                    "Failed to serialize banking event for outbox",
                    exception
            );
        }
    }
}