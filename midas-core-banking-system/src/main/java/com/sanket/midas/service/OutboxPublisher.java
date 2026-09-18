    package com.sanket.midas.service;

import com.sanket.midas.entity.OutboxEvent;
import com.sanket.midas.repository.OutboxEventRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class OutboxPublisher {

    private static final String TOPIC = "banking-events";

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, String> kafkaTemplate) {

        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(
            fixedDelayString = "${midas.outbox.publish-interval-ms:2000}"
    )
    public void publishPendingEvents() {

        List<OutboxEvent> pendingEvents =
                outboxEventRepository
                        .findByPublishedAtIsNullOrderByCreatedAtAsc();

        for (OutboxEvent event : pendingEvents) {

            try {

                kafkaTemplate.send(
                        TOPIC,
                        event.getAggregateId(),
                        event.getPayload()
                ).get(10, TimeUnit.SECONDS);

                event.markPublished();

                outboxEventRepository.save(event);

                System.out.println(
                        "Outbox event published: transaction "
                                + event.getTransactionId()
                );

            } catch (Exception exception) {

                System.err.println(
                        "Failed to publish outbox event: transaction "
                                + event.getTransactionId()
                );

                exception.printStackTrace();
            }
        }
    }
}