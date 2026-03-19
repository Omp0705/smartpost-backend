package com.om.smartpost.shipment.mapper;

import com.om.smartpost.shipment.dto.request.AddressRequest;
import com.om.smartpost.shipment.dto.response.AddressResponse;
import com.om.smartpost.shipment.entity.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public Address toEntity(AddressRequest request) {
        if (request == null) {
            return null;
        }

        Address address = new Address();
        address.setAddressType(request.addressType());
        address.setTitle(request.title());
        address.setFirstName(request.firstName());
        address.setMiddleName(request.middleName());
        address.setLastName(request.lastName());
        address.setEmail(request.email());
        address.setMobileNo(request.mobileNo());
        address.setCompanyName(request.companyName());
        address.setAddressLine1(request.addressLine1());
        address.setAddressLine2(request.addressLine2());
        address.setLandmark(request.landmark());
        address.setPincode(request.pincode());
        address.setCity(request.city());
        address.setState(request.state());
        return address;
    }

    public AddressResponse toResponse(Address address) {
        if (address == null) {
            return null;
        }

        return new AddressResponse(
                address.getAddressType(),
                address.getTitle(),
                address.getFirstName(),
                address.getMiddleName(),
                address.getLastName(),
                address.getEmail(),
                address.getMobileNo(),
                address.getCompanyName(),
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getLandmark(),
                address.getPincode(),
                address.getCity(),
                address.getState()
        );
    }
}
