package com.om.smartpost.notification.dto.mapper;


import com.om.smartpost.notification.dto.response.NotificationResponse;
import com.om.smartpost.notification.entities.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification entity) {
        if (entity == null) {
            return null;
        }

        return new NotificationResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getMessage(),
                entity.getLink(),
                entity.isRead(),
                entity.getCreatedAt(),
                entity.getType()
        );
    }
}