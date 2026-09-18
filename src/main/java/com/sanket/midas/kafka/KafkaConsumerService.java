package com.sanket.midas.kafka;

import com.sanket.midas.service.IncentiveService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Service
public class KafkaConsumerService {

    private final IncentiveService incentiveService;
    private final JsonMapper jsonMapper;

    public KafkaConsumerService(
            IncentiveService incentiveService,
            JsonMapper jsonMapper) {

        this.incentiveService = incentiveService;
        this.jsonMapper = jsonMapper;
    }

    @KafkaListener(
            topics = "banking-events",
            groupId = "midas-banking-consumer"
    )
    public void consumeEvent(String message) {

        try {

            BankingEvent event =
                    jsonMapper.readValue(
                            message,
                            BankingEvent.class
                    );

            System.out.println(
                    "Kafka event received: " + message
            );

            incentiveService.processEvent(event);

        } catch (JacksonException exception) {

            System.err.println(
                    "Failed to deserialize banking event: "
                            + message
            );

            exception.printStackTrace();
        }
    }
}