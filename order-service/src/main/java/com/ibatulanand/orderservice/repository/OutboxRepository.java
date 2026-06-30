package com.ibatulanand.orderservice.repository;

import com.ibatulanand.orderservice.model.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OutboxRepository extends JpaRepository<Outbox, String> {
    
    // Uses native MySQL 8.0 SKIP LOCKED for multi-pod concurrency safety
    @Query(value = "SELECT * FROM t_outbox WHERE status = 'PENDING' ORDER BY created_at ASC LIMIT 50 FOR UPDATE SKIP LOCKED", nativeQuery = true)
    List<Outbox> findPendingEventsForProcessing();

    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM Outbox o WHERE o.status = com.ibatulanand.orderservice.model.OutboxStatus.COMPLETED AND o.updatedAt < :cutoff")
    int deleteCompletedEventsOlderThan(@org.springframework.data.repository.query.Param("cutoff") java.time.LocalDateTime cutoff);
}
