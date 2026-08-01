package com.ibatulanand.notificationservice.service;

import com.ibatulanand.notificationservice.event.OrderPlacedEvent;
import com.ibatulanand.notificationservice.model.NotificationInbox;
import com.ibatulanand.notificationservice.repository.NotificationInboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationInboxRepository notificationInboxRepository;

    @KafkaListener(topics = "${app.kafka.topics.notification}")
    @Transactional
    public void consumeOrderPlacedEvent(OrderPlacedEvent orderPlacedEvent) {
        log.info("Received OrderPlacedEvent for order: {}", orderPlacedEvent.getOrderNumber());

        boolean alreadyProcessed = notificationInboxRepository.existsById(orderPlacedEvent.getOrderNumber());
        if (alreadyProcessed) {
            log.info("Order {} was already processed. Skipping to ensure idempotency.", orderPlacedEvent.getOrderNumber());
            return;
        }

        try {
            // Simulated business logic to send notification (e.g. Email/SMS)
            log.info("Sending notification for order: {} ...", orderPlacedEvent.getOrderNumber());
            
            // Mark as processed in the Inbox
            NotificationInbox inboxRecord = new NotificationInbox();
            inboxRecord.setOrderNumber(orderPlacedEvent.getOrderNumber());
            inboxRecord.setProcessedAt(LocalDateTime.now());
            
            notificationInboxRepository.save(inboxRecord);
            
            log.info("Successfully processed and saved to Inbox for order: {}", orderPlacedEvent.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to process OrderPlacedEvent for order: {}", orderPlacedEvent.getOrderNumber(), e);
            throw e; // Throw to allow Kafka to retry based on auto-commit settings or DLT
        }
    }
}
