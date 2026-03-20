package com.om.smartpost.notification.service;

import com.om.smartpost.notification.dto.mapper.NotificationMapper;
import com.om.smartpost.notification.dto.response.NotificationResponse;
import com.om.smartpost.notification.entities.Notification;
import com.om.smartpost.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(Long userId) {
        // Updated method call
        return notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }

//    @Transactional
//    public NotificationResponse updateNotificationStatus(Long notificationId, Long userId, NotificationUpdateRequest request) {
//        // Updated method call
//        Notification notification = notificationRepository.findByIdAndRecipientUserId(notificationId, userId)
//                .orElseThrow(() -> new RuntimeException("Notification not found or access denied."));
//
//        notification.setRead(request.getIsRead());
//        Notification savedNotification = notificationRepository.save(notification);
//
//        return notificationMapper.toResponse(savedNotification);
//    }

    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        // Updated method call
        Notification notification = notificationRepository.findByIdAndRecipientUserId(notificationId, userId)
                .orElseThrow(() -> new RuntimeException("Notification not found or access denied."));

        notificationRepository.delete(notification);
    }
}