package com.ibatulanand.orderservice.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibatulanand.orderservice.event.OrderPlacedEvent;
import com.ibatulanand.orderservice.model.Outbox;
import com.ibatulanand.orderservice.model.OutboxStatus;
import com.ibatulanand.orderservice.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxScheduler {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${app.outbox.poll.interval:5000}")
    @Transactional
    public void processOutboxEvents() {
        List<Outbox> pendingEvents = outboxRepository.findPendingEventsForProcessing();
        
        if (pendingEvents.isEmpty()) {
            return;
        }

        for (Outbox event : pendingEvents) {
            try {
                OrderPlacedEvent payload = objectMapper.readValue(event.getPayload(), OrderPlacedEvent.class);
                
                // Synchronous send (.get()) ensures we only mark as COMPLETED if the Kafka broker actually acknowledges
                kafkaTemplate.send("notificationTopic", payload).get();
                
                event.setStatus(OutboxStatus.COMPLETED);
                event.setErrorMessage(null);
                log.info("Successfully published outbox event: {}", event.getId());
            } catch (Exception e) {
                log.error("Failed to process outbox event id: {}", event.getId(), e);
                event.setRetryCount(event.getRetryCount() + 1);
                event.setLastAttemptAt(LocalDateTime.now());
                event.setErrorMessage(e.getMessage());
                
                if (event.getRetryCount() >= 3) {
                    event.setStatus(OutboxStatus.FAILED);
                    log.error("Outbox event {} permanently failed after 3 retries", event.getId());
                }
            }
        }
        // Hibernate dirty checking automatically flushes the updated Outbox entities at transaction commit.
    }

    @Scheduled(cron = "${app.outbox.cleanup.cron:0 0 0 * * ?}") // Runs at midnight every day by default
    @Transactional
    public void cleanupOutboxEvents() {
        // Default retention is 7 days unless overridden via properties
        int retentionDays = 7;
        try {
            String retentionProp = System.getProperty("app.outbox.cleanup.retention.days");
            if (retentionProp != null) {
                retentionDays = Integer.parseInt(retentionProp);
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid retention days property, defaulting to 7");
        }

        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        int deletedCount = outboxRepository.deleteCompletedEventsOlderThan(cutoff);
        
        if (deletedCount > 0) {
            log.info("Outbox Cleanup: Deleted {} COMPLETED events older than {}", deletedCount, cutoff);
        }
    }
}
