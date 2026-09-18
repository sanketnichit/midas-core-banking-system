package com.sanket.midas.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Service
public class KafkaProducerService {

    private static final String TOPIC = "banking-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;

    public KafkaProducerService(
            KafkaTemplate<String, String> kafkaTemplate,
            JsonMapper jsonMapper) {

        this.kafkaTemplate = kafkaTemplate;
        this.jsonMapper = jsonMapper;
    }

    public void sendEvent(BankingEvent event) {

        try {
            String message =
                    jsonMapper.writeValueAsString(event);

            kafkaTemplate.send(
                    TOPIC,
                    String.valueOf(event.accountId()),
                    message
            );

            System.out.println(
                    "Kafka event sent: " + message
            );

        } catch (JacksonException exception) {

            throw new IllegalStateException(
                    "Failed to serialize banking event",
                    exception
            );
        }
    }
}