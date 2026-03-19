package com.om.smartpost.shipment.dto.request;

import com.om.smartpost.shipment.enums.AreaType;
import com.om.smartpost.shipment.enums.DeliveryPriority;
import com.om.smartpost.shipment.enums.DeliverySlot;
import com.om.smartpost.shipment.enums.ServiceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ParcelCreateRequest {
    @NotNull(message = "Service Type is required")
    private ServiceType serviceType;

    @Valid
    private AddressRequest pickupAddress;
    @Valid
    private DropoffInfoRequest dropoffInfo;

    @NotBlank(message = "Receiver name is required")
    private String receiverName;

    @NotBlank(message = "Receiver Address Line 1 is required")
    private String receiverAddress1;

    private String receiverAddress2;

    @NotBlank(message = "Receiver Phone is required")
    @Pattern(regexp = "^\\d{10}$", message = "Receiver Phone must be 10 digits")
    private String receiverPhone;

    @NotBlank(message = "Receiver Pincode is required")
    @Pattern(regexp = "^\\d{6}$", message = "Receiver Pincode must be 6 digits")
    private String receiverPincode;

    // Optional User ID if receiver is a registered user
    private String receiverUserId;

    @NotNull(message = "Area Type is required")
    private AreaType areaType;

    @NotNull(message = "Delivery Priority is required")
    private DeliveryPriority deliveryPriority;

    @NotNull(message = "Weight is required")
    @Min(value = 0, message = "Weight must be positive")
    private Double weightKg;

    private boolean fragile;
    private BigDecimal codAmount;
    private DeliverySlot preferredSlot;
}



