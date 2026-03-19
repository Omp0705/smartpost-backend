package com.om.smartpost.shipment.dto.request;

import com.om.smartpost.shipment.enums.AreaType;
import com.om.smartpost.shipment.enums.DeliveryPriority;
import com.om.smartpost.shipment.enums.DeliverySlot;
import com.om.smartpost.shipment.enums.ServiceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ShipmentDataRequest(
        @NotNull ServiceType serviceType,
        @Valid AddressRequest pickupAddress,
        @Valid DropoffInfoRequest dropoffInfo,
        @NotNull AreaType areaType,
        @NotNull DeliveryPriority deliveryPriority,
        @NotBlank(message = "Origin pincode is required") String originPincode,
        @NotBlank(message = "Origin Post Office name is required") String originPoName,
        @NotBlank(message = "Destination pincode is required") String destinationPincode,
        @NotBlank(message = "Destination Post Office name is required") String destinationPoName,
        @NotNull @Positive Double weightKg,
        boolean fragile,
        @Positive Double lengthCms,
        @Positive Double widthCms,
        @Positive Double heightCms,
        BigDecimal codAmount,
        DeliverySlot preferredSlot,
        DeliverySlot predictedSlot
) {
}



