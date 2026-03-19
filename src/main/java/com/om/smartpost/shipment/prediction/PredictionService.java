package com.om.smartpost.shipment.prediction;

import com.om.smartpost.core.location.GeocodingService;
import com.om.smartpost.shipment.entity.Address;
import com.om.smartpost.shipment.entity.DropoffInfo;
import com.om.smartpost.shipment.entity.Shipment;
import com.om.smartpost.shipment.entity.ShipmentContact;
import com.om.smartpost.shipment.enums.AddressType;
import com.om.smartpost.shipment.enums.DeliveryPriority;
import com.om.smartpost.shipment.enums.DeliverySlot;
import com.om.smartpost.shipment.enums.ShipmentStatus;
import com.om.smartpost.shipment.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PredictionService {

    private static final String RECEIVER_PHONE_PREFIX = "PHONE_RECEIVER_";

    private final GeocodingService geocodingService;
    private final PredictionClient predictionClient;
    private final EtaEstimator etaEstimator;
    private final ShipmentRepository shipmentRepository;
    private final SlotMapper slotMapper;

    public void populatePredictionDetails(Shipment shipment) {
        PredictionOutcome outcome = predict(shipment);
        shipment.setPredictedSlot(outcome.getPredictedSlot());
        shipment.setDeliveryDate(outcome.getDeliveryDate());
    }

    public PredictionOutcome predict(Shipment shipment) {
        List<DeliverySlot> candidateSlots = Arrays.asList(DeliverySlot.values());
        DeliverySlot fallbackSlot = resolveFallbackSlot(shipment.getPreferredSlot(), candidateSlots);
        LocalDate bookingDate = resolveBookingDate(shipment);
        LocalDate deliveryDate = estimateDeliveryDate(shipment, bookingDate);

        try {
            PredictionPayload payload = buildPayload(shipment, candidateSlots, bookingDate, deliveryDate);
            PredictionClientResponse response = predictionClient.predict(payload);
            return PredictionOutcome.builder()
                    .predictedSlot(slotMapper.fromWireFormat(response.resolveSlot()))
                    .deliveryDate(deliveryDate)
                    .build();
        } catch (Exception ex) {
            log.warn("Prediction failed for shipment create flow. Falling back to deterministic slot. phone={}",
                    shipment.getReceiverDetails() != null ? shipment.getReceiverDetails().getMobileNo() : null, ex);
            return PredictionOutcome.builder()
                    .predictedSlot(fallbackSlot)
                    .deliveryDate(deliveryDate)
                    .build();
        }
    }

    PredictionPayload buildPayload(Shipment shipment, List<DeliverySlot> candidateSlots, LocalDate bookingDate, LocalDate deliveryDate) {
        ShipmentContact receiverDetails = shipment.getReceiverDetails();
        String deliveryPhone = receiverDetails != null ? receiverDetails.getMobileNo() : null;
        DeliverySlot initialSenderSlot = resolveFallbackSlot(shipment.getPreferredSlot(), candidateSlots);
        GeocodingResult destination = safeGeocodeDestination(receiverDetails);

        return PredictionPayload.builder()
                .customerId(resolveCustomerId(shipment, deliveryPhone))
                .area(resolveArea(destination, receiverDetails)) // Gets "Airoli" from Google!
                .pinCodeCluster(receiverDetails != null ? receiverDetails.getPincode() : null)
                .addressType(resolveAddressType(shipment).name())
                .initialSenderSlot(slotMapper.toWireFormat(initialSenderSlot))
                .bookingDate(bookingDate)
                .deliveryDate(deliveryDate)
                .previousFailedAttempts(deliveryPhone != null
                        ? shipmentRepository.countByReceiverDetails_MobileNoAndCurrentStatus(deliveryPhone, ShipmentStatus.DELIVERY_FAILED)
                        : 0)
                .attemptNumber(1)
                .candidateSlots(candidateSlots.stream().map(slotMapper::toWireFormat).toList())
                .build();
    }

    private LocalDate estimateDeliveryDate(Shipment shipment, LocalDate bookingDate) {
        GeocodingResult destination = safeGeocodeDestination(shipment.getReceiverDetails());
        GeocodingResult origin = safeGeocodeOrigin(shipment);
        RouteTier routeTier = determineRouteTier(origin, destination);
        return etaEstimator.estimate(bookingDate, resolvePriority(shipment.getDeliveryPriority()), routeTier);
    }

    private LocalDate resolveBookingDate(Shipment shipment) {
        return shipment.getBookingDate() != null ? shipment.getBookingDate() : LocalDate.now();
    }

    private GeocodingResult safeGeocodeDestination(ShipmentContact receiverDetails) {
        try {
            String query = buildAddressQuery(receiverDetails);
            return geocodingService.geocodeAddress(query, receiverDetails != null ? receiverDetails.getPincode() : null);
        } catch (Exception ex) {
            log.warn("Destination geocoding failed. pincode={}", receiverDetails != null ? receiverDetails.getPincode() : null, ex);
            return GeocodingResult.builder()
                    .pincode(receiverDetails != null ? receiverDetails.getPincode() : null)
                    .build();
        }
    }

    private GeocodingResult safeGeocodeOrigin(Shipment shipment) {
        try {
            if (shipment.getPickupAddress() != null) {
                return geocodingService.geocodeAddress(buildAddressQuery(shipment.getPickupAddress()), shipment.getPickupAddress().getPincode());
            }
            DropoffInfo dropoffInfo = shipment.getDropoffInfo();
            return geocodingService.geocodePincode(dropoffInfo != null ? dropoffInfo.getPincode() : null);
        } catch (Exception ex) {
            log.warn("Origin geocoding failed for shipment serviceType={}", shipment.getServiceType(), ex);
            return GeocodingResult.builder().build();
        }
    }

    private RouteTier determineRouteTier(GeocodingResult origin, GeocodingResult destination) {
        if (destination == null || destination.isRemote()) {
            return RouteTier.REMOTE;
        }
        if (origin == null || !origin.hasCityAndState() || !destination.hasCityAndState()) {
            return RouteTier.REMOTE;
        }
        if (origin.getCity().equalsIgnoreCase(destination.getCity())
                && origin.getState().equalsIgnoreCase(destination.getState())) {
            return RouteTier.LOCAL;
        }
        if (origin.getState().equalsIgnoreCase(destination.getState())) {
            return RouteTier.INTRA_STATE;
        }
        return RouteTier.INTER_STATE;
    }

    private String resolveCustomerId(Shipment shipment, String deliveryPhone) {
        if (shipment.getReceiverUser() != null && shipment.getReceiverUser().getUserId() != null) {
            return String.valueOf(shipment.getReceiverUser().getUserId());
        }
        return RECEIVER_PHONE_PREFIX + deliveryPhone;
    }

    private String resolveArea(GeocodingResult destination, ShipmentContact receiverDetails) {
        if (destination != null && destination.getArea() != null && !destination.getArea().isBlank()) {
            return destination.getArea();
        }
        return receiverDetails != null ? receiverDetails.getPincode() : "UNKNOWN";
    }

    private AddressType resolveAddressType(Shipment shipment) {
        Address pickupAddress = shipment.getPickupAddress();
        if (pickupAddress != null && pickupAddress.getAddressType() != null) {
            return pickupAddress.getAddressType();
        }
        return AddressType.HOME;
    }

    private DeliveryPriority resolvePriority(DeliveryPriority priority) {
        return priority != null ? priority : DeliveryPriority.NORMAL;
    }

    private DeliverySlot resolveFallbackSlot(DeliverySlot preferredSlot, List<DeliverySlot> candidateSlots) {
        return preferredSlot != null ? preferredSlot : candidateSlots.get(0);
    }

    // --- UPDATED METHODS BELOW ---

    private String buildAddressQuery(ShipmentContact receiverDetails) {
        if (receiverDetails == null) {
            return null;
        }
        // Added Landmark and cleaned up double commas
        return String.join(", ",
                safeValue(receiverDetails.getAddressLine1()),
                safeValue(receiverDetails.getAddressLine2()),
                safeValue(receiverDetails.getLandmark()),
                safeValue(receiverDetails.getCity()),
                safeValue(receiverDetails.getState()),
                safeValue(receiverDetails.getPincode())
        ).replaceAll(" ,", ",").replaceAll(",,", ",").trim();
    }

    private String buildAddressQuery(Address address) {
        if (address == null) {
            return null;
        }
        // Added Landmark and cleaned up double commas
        return String.join(", ",
                safeValue(address.getAddressLine1()),
                safeValue(address.getAddressLine2()),
                safeValue(address.getLandmark()),
                safeValue(address.getCity()),
                safeValue(address.getState()),
                safeValue(address.getPincode())
        ).replaceAll(" ,", ",").replaceAll(",,", ",").trim();
    }

    private String safeValue(String value) {
        // Ensures spaces aren't treated as valid strings
        return (value == null || value.trim().isEmpty()) ? "" : value.trim();
    }
}