package com.om.smartpost.shipment.prediction;

import com.om.smartpost.shipment.enums.DeliverySlot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlotMapperTest {

    private final SlotMapper slotMapper = new SlotMapper();

    @Test
    void toWireFormatRemovesPrefix() {
        assertEquals("10_12", slotMapper.toWireFormat(DeliverySlot.SLOT_10_12));
    }

    @Test
    void fromWireFormatAddsPrefix() {
        assertEquals(DeliverySlot.SLOT_10_12, slotMapper.fromWireFormat("10_12"));
    }
}

