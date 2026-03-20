package com.om.smartpost.office.postoffice.mapper;

import com.om.smartpost.office.postoffice.dto.request.PostOfficeRequest;
import com.om.smartpost.office.postoffice.dto.response.PostOfficeResponse;
import com.om.smartpost.office.postoffice.entity.PostOffice;
import com.om.smartpost.office.postoffice.enums.BranchType;
import com.om.smartpost.office.postoffice.enums.OfficeType;
import org.springframework.stereotype.Component;

@Component
public class PostOfficeMapper {

    public PostOffice toEntity(PostOfficeRequest request) {
        PostOffice office = new PostOffice();
        apply(office, request);
        return office;
    }

    public void apply(PostOffice office, PostOfficeRequest request) {
        office.setName(request.getName());
        office.setDescription(request.getDescription());
        office.setCircle(request.getCircle());
        office.setDistrict(request.getDistrict());
        office.setDivision(request.getDivision());
        office.setRegion(request.getRegion());
        office.setBlock(request.getBlock());
        office.setState(request.getState());
        office.setCountry(request.getCountry());
        office.setPincode(request.getPincode());
    }

    public PostOfficeResponse toResponse(PostOffice office) {
        return new PostOfficeResponse(
                office.getId(),
                office.getName(),
                office.getDescription(),
                office.getBranchType().name(),
                office.getDeliveryStatus().name(),
                office.getCircle(),
                office.getDistrict(),
                office.getDivision(),
                office.getRegion(),
                office.getBlock(),
                office.getState(),
                office.getCountry(),
                office.getPincode(),
                office.getAddress(),
                office.getLatitude(),
                office.getLongitude(),
                office.getCreatedAt(),
                office.getUpdatedAt()
        );
    }
}


