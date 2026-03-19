package com.om.smartpost.shipment.notification.entity;

import com.om.smartpost.core.identity.entity.User;

import com.om.smartpost.shipment.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User recipient; // The receiver of the shipment

    private String title;
    private String message;
    private String link; // For SMS users or deep-linking in app

    private boolean isRead = false;
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private NotificationType type; // e.g., SMS, PUSH, BOTH
}



