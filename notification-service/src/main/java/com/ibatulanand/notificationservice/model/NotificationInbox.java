package com.ibatulanand.notificationservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_notification_inbox")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationInbox {
    
    @Id
    private String orderNumber;
    
    private LocalDateTime processedAt;
}
