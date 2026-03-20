package com.om.smartpost.notification.repository;

import com.om.smartpost.notification.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification,Long> {

    // Changed from RecipientId to RecipientUserId
    List<Notification> findByRecipientUserIdOrderByCreatedAtDesc(Long userId);

    // Changed from RecipientId to RecipientUserId
    Optional<Notification> findByIdAndRecipientUserId(Long id, Long userId);
}
