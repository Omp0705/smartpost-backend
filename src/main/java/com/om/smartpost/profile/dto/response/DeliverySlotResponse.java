package com.om.smartpost.profile.dto.response;

import com.om.smartpost.shipment.enums.DeliverySlot;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeliverySlotResponse {
    private String code;
    private String label;

    public static DeliverySlotResponse fromEnum(DeliverySlot slot) {
        if (slot == null) return null;
        return new DeliverySlotResponse(slot.name(), slot.getLabel());
    }
}



