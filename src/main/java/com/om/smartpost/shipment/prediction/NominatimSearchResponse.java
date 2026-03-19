package com.om.smartpost.shipment.prediction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NominatimSearchResponse {
    @JsonProperty("addresstype")
    private String addressType;

    @JsonProperty("type")
    private String resultType;

    @JsonProperty("address")
    private NominatimAddress address;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NominatimAddress {
        private String suburb;
        private String neighbourhood;
        private String quarter;
        private String city;
        private String town;
        private String village;
        private String municipality;
        private String county;
        private String state;
        private String postcode;
    }
}

