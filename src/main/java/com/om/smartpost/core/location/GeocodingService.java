package com.om.smartpost.core.location;

import com.om.smartpost.shipment.prediction.GeocodingResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Service
public class GeocodingService {

    private final RestClient geocodingRestClient;
    private final String googleApiKey;

    public GeocodingService(
            @Qualifier("geocodingRestClient") RestClient geocodingRestClient,
            @Value("${google.maps.api-key}") String googleApiKey) {
        this.geocodingRestClient = geocodingRestClient;
        this.googleApiKey = googleApiKey;
    }

    public GeocodingResult geocodeAddress(String addressQuery, String fallbackPincode) {
        if (addressQuery == null || addressQuery.isBlank()) {
            return geocodePincode(fallbackPincode);
        }
        return findBestMatch(addressQuery, fallbackPincode);
    }

    public GeocodingResult geocodePincode(String pincode) {
        if (pincode == null || pincode.isBlank()) {
            return GeocodingResult.builder().build();
        }
        return findBestMatch(pincode, pincode);
    }

    private GeocodingResult findBestMatch(String query, String fallbackPincode) {
        try {
            GoogleGeocodeResponse response = geocodingRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("maps.googleapis.com")
                            .path("/maps/api/geocode/json")
                            .queryParam("address", query)
                            .queryParam("key", googleApiKey)
                            .build())
                    .retrieve()
                    .body(GoogleGeocodeResponse.class);

            if (response == null || !"OK".equals(response.status()) || response.results().isEmpty()) {
                return GeocodingResult.builder().pincode(fallbackPincode).build();
            }

            return mapGoogleResponse(response.results().get(0), fallbackPincode);

        } catch (RestClientException ex) {
            throw new IllegalStateException("Google Geocoding request failed", ex);
        }
    }

    private GeocodingResult mapGoogleResponse(GoogleGeocodeResponse.Result result, String fallbackPincode) {
        Double lat = result.geometry().location().lat();
        Double lng = result.geometry().location().lng();

        String area = getComponent(result, "sublocality_level_1", "sublocality", "neighborhood");
        String city = getComponent(result, "locality", "administrative_area_level_2");
        String state = getComponent(result, "administrative_area_level_1");
        String pincode = getComponent(result, "postal_code");

        boolean isRemote = (city == null || city.isBlank());

        return GeocodingResult.builder()
                .latitude(lat)
                .longitude(lng)
                .area(area != null ? area : city)
                .city(city)
                .state(state)
                .pincode(pincode != null ? pincode : fallbackPincode)
                .remote(isRemote)
                .build();
    }

    // THIS WAS MISSING: The helper method to extract data safely
    private String getComponent(GoogleGeocodeResponse.Result result, String... targetTypes) {
        for (String targetType : targetTypes) {
            for (GoogleGeocodeResponse.AddressComponent comp : result.address_components()) {
                if (comp.types().contains(targetType)) {
                    return comp.long_name();
                }
            }
        }
        return null;
    }
}

// THIS WAS MISSING: The DTOs to map Google's JSON response
