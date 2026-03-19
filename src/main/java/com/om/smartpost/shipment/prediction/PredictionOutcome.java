package com.om.smartpost.shipment.prediction;

import com.om.smartpost.shipment.enums.DeliverySlot;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class PredictionOutcome {
    DeliverySlot predictedSlot;
    LocalDate deliveryDate;
}

