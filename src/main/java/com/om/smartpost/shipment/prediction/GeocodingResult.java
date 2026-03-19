package com.om.smartpost.shipment.prediction;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GeocodingResult {
    private String area;      // Captures "Airoli", "Dadar", etc.
    private String city;
    private String state;
    private String pincode;
    private boolean remote;
    private Double latitude;
    private Double longitude;

    public boolean isRemote() {
        return remote;
    }

    public boolean hasCityAndState() {
        return city != null && !city.isBlank() && state != null && !state.isBlank();
    }
}