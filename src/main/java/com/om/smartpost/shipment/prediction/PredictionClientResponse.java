package com.om.smartpost.shipment.prediction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PredictionClientResponse {
    @JsonProperty("model_version")
    private String modelVersion;

    @JsonProperty("recommended_slot")
    private SlotRecommendation recommendedSlot;

    @JsonProperty("fallback_slot")
    private SlotRecommendation fallbackSlot;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SlotRecommendation {
        private String slot;
        private Double probability;
    }

    public String resolveSlot() {
        if (recommendedSlot != null && recommendedSlot.getSlot() != null && !recommendedSlot.getSlot().isBlank()) {
            return recommendedSlot.getSlot();
        }
        if (fallbackSlot != null && fallbackSlot.getSlot() != null && !fallbackSlot.getSlot().isBlank()) {
            return fallbackSlot.getSlot();
        }
        return null;
    }
}

