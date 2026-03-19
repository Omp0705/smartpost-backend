package com.om.smartpost.shipment.mapper;

import com.om.smartpost.shipment.dto.request.DropoffInfoRequest;
import com.om.smartpost.shipment.dto.response.DropoffInfoResponse;
import com.om.smartpost.shipment.entity.DropoffInfo;
import org.springframework.stereotype.Component;

@Component
public class DropoffInfoMapper {

    public DropoffInfo toEntity(DropoffInfoRequest request) {
        if (request == null) {
            return null;
        }

        DropoffInfo dropoffInfo = new DropoffInfo();
        dropoffInfo.setDropoffOffice(request.dropoffOffice());
        dropoffInfo.setPincode(request.pincode());
        return dropoffInfo;
    }

    public DropoffInfoResponse toResponse(DropoffInfo dropoffInfo) {
        if (dropoffInfo == null) {
            return null;
        }

        return new DropoffInfoResponse(dropoffInfo.getDropoffOffice(), dropoffInfo.getPincode());
    }
}

