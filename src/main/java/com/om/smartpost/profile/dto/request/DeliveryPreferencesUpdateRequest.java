package com.om.smartpost.profile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPreferencesUpdateRequest {

    private DeliverySlotRequest preferredDeliverySlot;
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



