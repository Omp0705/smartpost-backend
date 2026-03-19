package com.om.smartpost.shipment.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ShipmentCreateRequest(
        @Valid @NotNull ShipmentContactRequest senderDetails,
        @Valid @NotNull ShipmentContactRequest receiverDetails,
        @Valid @NotNull ShipmentDataRequest shipmentData
) {
}



