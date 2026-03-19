package com.om.smartpost.shipment.prediction;

import com.om.smartpost.shipment.entity.Shipment;
import com.om.smartpost.shipment.entity.ShipmentContact;
import com.om.smartpost.shipment.enums.DeliveryPriority;
import com.om.smartpost.shipment.enums.DeliverySlot;
import com.om.smartpost.shipment.enums.ServiceType;
import com.om.smartpost.shipment.enums.ShipmentStatus;
import com.om.smartpost.shipment.repository.ShipmentRepository;
import com.om.smartpost.shipment.prediction.SlotMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PredictionServiceTest {

    @Mock
    private GeocodingService geocodingService;
    @Mock
    private PredictionClient predictionClient;
    @Mock
    private ShipmentRepository shipmentRepository;

    private final EtaEstimator etaEstimator = new EtaEstimator();
    private final SlotMapper slotMapper = new SlotMapper();

    @InjectMocks
    private PredictionService predictionService;

    @Test
    void returnsPredictedSlotOnSuccess() {
        Shipment shipment = buildShipment();
        when(geocodingService.geocodeAddress(any(), eq("110019")))
                .thenReturn(GeocodingResult.builder().area("Nehru Place").city("Delhi").state("Delhi").pincode("110019").build());
        when(geocodingService.geocodeAddress(any(), eq("110001")))
                .thenReturn(GeocodingResult.builder().area("Connaught Place").city("Delhi").state("Delhi").pincode("110001").build());
        when(shipmentRepository.countByReceiverDetails_MobileNoAndCurrentStatus("9999999999", ShipmentStatus.DELIVERY_FAILED))
                .thenReturn(2L);
        PredictionClientResponse response = new PredictionClientResponse();
        PredictionClientResponse.SlotRecommendation recommendation = new PredictionClientResponse.SlotRecommendation();
        recommendation.setSlot("12_02");
        recommendation.setProbability(0.91);
        response.setRecommendedSlot(recommendation);
        when(predictionClient.predict(any())).thenReturn(response);

        PredictionService service = new PredictionService(geocodingService, predictionClient, etaEstimator, shipmentRepository, slotMapper);

        PredictionOutcome outcome = service.predict(shipment);

        assertEquals(DeliverySlot.SLOT_12_02, outcome.getPredictedSlot());
        assertNotNull(outcome.getDeliveryDate());
    }

    @Test
    void fallsBackToPreferredSlotWhenPredictionFails() {
        Shipment shipment = buildShipment();
        doThrow(new IllegalStateException("boom")).when(predictionClient).predict(any());
        when(geocodingService.geocodeAddress(any(), eq("110019")))
                .thenReturn(GeocodingResult.builder().area("Nehru Place").city("Delhi").state("Delhi").pincode("110019").build());
        when(geocodingService.geocodeAddress(any(), eq("110001")))
                .thenReturn(GeocodingResult.builder().area("Connaught Place").city("Delhi").state("Delhi").pincode("110001").build());

        PredictionService service = new PredictionService(geocodingService, predictionClient, etaEstimator, shipmentRepository, slotMapper);

        assertEquals(DeliverySlot.SLOT_10_12, service.predict(shipment).getPredictedSlot());
    }

    @Test
    void fallsBackToFirstCandidateWhenPreferredSlotMissingAndGeocodingFails() {
        Shipment shipment = buildShipment();
        shipment.setPreferredSlot(null);
        doThrow(new IllegalStateException("geo down")).when(geocodingService).geocodeAddress(any(), any());

        PredictionService service = new PredictionService(geocodingService, predictionClient, etaEstimator, shipmentRepository, slotMapper);

        assertEquals(DeliverySlot.SLOT_10_12, service.predict(shipment).getPredictedSlot());
    }

    @Test
    void buildPayloadUsesFallbackWhenPreferredSlotMissing() {
        Shipment shipment = buildShipment();
        shipment.setPreferredSlot(null);
        when(geocodingService.geocodeAddress(any(), eq("110019")))
                .thenReturn(GeocodingResult.builder().area("Nehru Place").city("Delhi").state("Delhi").pincode("110019").build());
        when(geocodingService.geocodeAddress(any(), eq("110001")))
                .thenReturn(GeocodingResult.builder().area("Connaught Place").city("Delhi").state("Delhi").pincode("110001").build());

        PredictionService service = new PredictionService(geocodingService, predictionClient, etaEstimator, shipmentRepository, slotMapper);
        PredictionPayload payload = service.buildPayload(
                shipment,
                java.util.Arrays.asList(DeliverySlot.values()),
                shipment.getBookingDate(),
                java.time.LocalDate.of(2026, 3, 14));

        assertNotNull(payload.getInitialSenderSlot());
        assertEquals("10_12", payload.getInitialSenderSlot());
    }

    private Shipment buildShipment() {
        Shipment shipment = new Shipment();
        shipment.setServiceType(ServiceType.PICKUP);
        shipment.setDeliveryPriority(DeliveryPriority.SPEED);
        shipment.setPreferredSlot(DeliverySlot.SLOT_10_12);
        shipment.setBookingDate(java.time.LocalDate.of(2026, 3, 12));

        ShipmentContact receiverDetails = ShipmentContact.builder()
                .fname("Receiver")
                .lName("Two")
                .mobileNo("9999999999")
                .addressLine1("Nehru Place")
                .addressLine2("South Delhi")
                .city("Delhi")
                .state("Delhi")
                .pincode("110019")
                .build();
        shipment.setReceiverDetails(receiverDetails);

        com.om.smartpost.shipment.entity.Address address = new com.om.smartpost.shipment.entity.Address();
        address.setAddressLine1("Connaught Place");
        address.setPincode("110001");
        address.setCity("Delhi");
        address.setState("Delhi");
        shipment.setPickupAddress(address);
        return shipment;
    }
}


