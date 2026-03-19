package com.om.smartpost.shipment.repository;

import com.om.smartpost.shipment.entity.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TrackingEventRepository  extends JpaRepository<TrackingEvent, UUID> {
}
