package com.om.smartpost.shipment.dto.response;

import com.om.smartpost.shipment.enums.AreaType;
import com.om.smartpost.shipment.enums.DeliveryPriority;
import com.om.smartpost.shipment.enums.DeliverySlot;
import com.om.smartpost.shipment.enums.ServiceType;
import com.om.smartpost.shipment.enums.ShipmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ShipmentResponse(
        UUID id,
        String trackingNumber,
        String articleBarcode,
        ServiceType serviceType,
        AddressResponse pickupAddress,
        DropoffInfoResponse dropoffInfo,
        Long senderUserId,
        Long receiverUserId,
        ShipmentContactResponse senderDetails,
        ShipmentContactResponse receiverDetails,
        AreaType areaType,
        String destinationPoName,
        String destinationPincode,
        String originPoName,
        String originPincode,
        DeliveryPriority deliveryPriority,
        ShipmentStatus currentStatus,
        Double weightKg,
        boolean fragile,
        boolean merchandise,
        Double lengthCms,
        Double widthCms,
        Double heightCms,
        Double physicalWeightGms,
        Double volumetricWeightGms,
        Double chargedWeightGms,
        BigDecimal codAmount,
        DeliverySlot preferredSlot,
        DeliverySlot predictedSlot,
        LocalDate bookingDate,
        LocalDate deliveryDate,
        UUID beatId,
        String beatCode,
        String postmanEmployeeId,
        String postmanName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ShipmentTrackingEventResponse> trackingHistory
) {
}



