package com.om.smartpost.shipment.notification.repository;

import com.om.smartpost.shipment.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
}

