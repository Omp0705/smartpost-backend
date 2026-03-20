package com.om.smartpost.notification.dto.response;

import com.om.smartpost.notification.enums.NotificationType;
import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String title,
        String message,
        String link,
        boolean isRead,
        LocalDateTime createdAt,
        NotificationType type
) {}