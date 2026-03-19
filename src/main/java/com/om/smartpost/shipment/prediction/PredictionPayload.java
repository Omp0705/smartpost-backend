package com.om.smartpost.shipment.prediction;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;

@Value
@Builder
public class PredictionPayload {
    @JsonProperty("customer_id")
    String customerId;

    String area;

    @JsonProperty("pin_code_cluster")
    String pinCodeCluster;

    @JsonProperty("address_type")
    String addressType;

    @JsonProperty("initial_sender_slot")
    String initialSenderSlot;

    @JsonProperty("booking_date")
    LocalDate bookingDate;

    @JsonProperty("delivery_date")
    LocalDate deliveryDate;

    @JsonProperty("previous_failed_attempts")
    long previousFailedAttempts;

    @JsonProperty("attempt_number")
    int attemptNumber;

    @JsonProperty("candidate_slots")
    List<String> candidateSlots;
}

