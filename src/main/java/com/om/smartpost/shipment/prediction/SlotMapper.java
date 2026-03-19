package com.om.smartpost.shipment.prediction;

import com.om.smartpost.shipment.enums.DeliverySlot;
import org.springframework.stereotype.Component;

@Component
public class SlotMapper {

    public String toWireFormat(DeliverySlot slot) {
        return slot.name().replace("SLOT_", "");
    }

    public DeliverySlot fromWireFormat(String slot) {
        return DeliverySlot.valueOf("SLOT_" + slot);
    }
}

