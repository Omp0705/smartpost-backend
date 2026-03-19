package com.om.smartpost.core.location;

import java.util.List;

record GoogleGeocodeResponse(String status, List<Result> results) {
    public record Result(Geometry geometry, List<AddressComponent> address_components, String formatted_address) {}
    public record Geometry(Location location) {}
    public record Location(Double lat, Double lng) {}
    public record AddressComponent(String long_name, String short_name, List<String> types) {}
}