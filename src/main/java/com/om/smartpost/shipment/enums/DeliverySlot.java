package com.om.smartpost.shipment.enums;

import lombok.Getter;

@Getter
public enum DeliverySlot {
    SLOT_10_12("10:00 AM - 12:00 PM"),
    SLOT_12_02("12:00 PM - 02:00 PM"),
    SLOT_02_04("02:00 PM - 04:00 PM"),
    SLOT_04_06("04:00 PM - 06:00 PM"); // India Post often delivers until 6 PM

    private final String label;

    DeliverySlot(String label) {
        this.label = label;
    }

}


