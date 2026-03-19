package com.om.smartpost.shipment.dto.response;

import com.om.smartpost.shipment.enums.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ParcelResponse {

    private UUID id;
    private String trackingNumber;
    private ServiceType serviceType;
    private ShipmentStatus shipmentStatus;

    // Nested Objects
    private AddressResponse pickupAddress;
    private DropoffInfoResponse dropoffInfo;

    // Flat Receiver Details
    private String receiverName;
    private String receiverAddress1;
    private String receiverAddress2;
    private String receiverPhone;
    private String receiverPincode;

    // Specs
    private AreaType areaType;
    private DeliveryPriority deliveryPriority;
    private Double weightKg;
    private boolean fragile;
    private BigDecimal codAmount;

    // Slots
    private DeliverySlot preferredSlot;
    private DeliverySlot predictedSlot;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}




