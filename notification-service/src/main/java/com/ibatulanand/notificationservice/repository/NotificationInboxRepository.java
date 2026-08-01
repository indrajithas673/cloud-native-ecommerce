package com.ibatulanand.notificationservice.repository;

import com.ibatulanand.notificationservice.model.NotificationInbox;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationInboxRepository extends JpaRepository<NotificationInbox, String> {
}
