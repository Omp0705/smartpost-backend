package com.om.smartpost.shipment.dto.request;

import com.om.smartpost.shipment.enums.DeliverySlot;
import com.om.smartpost.shipment.enums.ShipmentStatus;
import lombok.Data;

@Data
public class ParcelUpdateRequest {
    private ShipmentStatus shipmentStatus;
    private Double weightKg;
    private DeliverySlot predictedSlot;
}



