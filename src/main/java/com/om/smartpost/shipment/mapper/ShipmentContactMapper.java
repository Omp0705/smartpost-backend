package com.om.smartpost.shipment.mapper;

import com.om.smartpost.shipment.dto.request.ShipmentContactRequest;
import com.om.smartpost.shipment.dto.response.ShipmentContactResponse;
import com.om.smartpost.shipment.entity.ShipmentContact;
import org.springframework.stereotype.Component;

@Component
public class ShipmentContactMapper {

    public ShipmentContact toEntity(ShipmentContactRequest request) {
        if (request == null) {
            return null;
        }

        return ShipmentContact.builder()
                .title(request.title())
                .fname(request.fname())
                .mName(request.mName())
                .lName(request.lName())
                .mobileNo(request.mobileNo())
                .email(request.email())
                .companyName(request.companyName())
                .addressLine1(request.addressLine1())
                .addressLine2(request.addressLine2())
                .landmark(request.landmark())
                .pincode(request.pincode())
                .city(request.city())
                .state(request.state())
                .dac(request.dac())
                .build();
    }

    public ShipmentContactResponse toResponse(ShipmentContact contact) {
        if (contact == null) {
            return null;
        }

        return new ShipmentContactResponse(
                contact.getTitle(),
                contact.getFname(),
                contact.getMName(),
                contact.getLName(),
                contact.getMobileNo(),
                contact.getEmail(),
                contact.getCompanyName(),
                contact.getAddressLine1(),
                contact.getAddressLine2(),
                contact.getLandmark(),
                contact.getPincode(),
                contact.getCity(),
                contact.getState(),
                contact.getDac(),
                contact.getLatitude(),
                contact.getLongitude()
        );
    }
}


