package com.om.smartpost.office.beat.mapper;

import com.om.smartpost.office.beat.dto.request.BeatRequest;
import com.om.smartpost.office.beat.dto.response.BeatResponse;
import com.om.smartpost.office.beat.entity.Beat;
import org.springframework.stereotype.Component;

@Component
public class BeatMapper {

    public Beat toEntity(BeatRequest request) {
        Beat beat = new Beat();
        apply(beat, request);
        return beat;
    }

    public void apply(Beat beat, BeatRequest request) {
        beat.setBeatCode(request.getBeatCode());
        beat.setName(request.getName());
        beat.setDescription(request.getDescription());

        beat.setAreaKeywords(request.getAreaKeywords());

        beat.setRouteOrder(request.getRouteOrder());
        beat.setActive(request.getActive() != null ? request.getActive() : Boolean.TRUE);
    }

    public BeatResponse toResponse(Beat beat) {
        return new BeatResponse(
                beat.getId(),
                beat.getOffice().getId(),
                beat.getOffice().getName(),
                beat.getBeatCode(),
                beat.getName(),
                beat.getDescription(),
                beat.getAreaKeywords(),
                beat.getRouteOrder(),
                beat.getActive(),
                beat.getAssignedPostman() != null ? beat.getAssignedPostman().getId() : null,
                beat.getAssignedPostman() != null ? beat.getAssignedPostman().getEmployeeId() : null,
                beat.getAssignedPostman() != null && beat.getAssignedPostman().getUser() != null
                        ? beat.getAssignedPostman().getUser().getFullName()
                        : null,
                beat.getShipments() != null ? beat.getShipments().size() : 0
        );
    }
}

