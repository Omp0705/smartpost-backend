package com.om.smartpost.shipment.events;

import com.om.smartpost.shipment.entity.Shipment;


public record ShipmentCreatedEvent(Shipment shipment) {}
