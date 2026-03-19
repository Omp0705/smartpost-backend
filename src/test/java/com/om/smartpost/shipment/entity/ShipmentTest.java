package com.om.smartpost.shipment.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShipmentTest {

    @Test
    void onCreateInitializesAuditTimestamps() {
        Shipment shipment = new Shipment();

        shipment.onCreate();

        assertNotNull(shipment.getCreatedAt());
        assertNotNull(shipment.getUpdatedAt());
        assertTrue(!shipment.getUpdatedAt().isBefore(shipment.getCreatedAt()));
    }

    @Test
    void trackingEventOnCreateInitializesTimestamp() {
        TrackingEvent event = new TrackingEvent();

        event.onCreate();

        assertNotNull(event.getTimestamp());
    }
}

