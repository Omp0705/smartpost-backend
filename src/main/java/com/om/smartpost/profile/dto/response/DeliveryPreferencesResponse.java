package com.om.smartpost.profile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeliveryPreferencesResponse {

    private DeliverySlotResponse preferredDeliverySlot;

    private Boolean leaveAtDoor;
    private Boolean leaveWithGuard;
    private Boolean deliverToNeighbor;
    private Boolean callBeforeDelivery;
    private Boolean otpRequired;
    private Boolean signatureRequired;
    private Boolean avoidMorning;
    private Boolean weekendOnly;

    private String deliveryNote;
}



