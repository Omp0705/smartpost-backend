package com.om.smartpost.office.staff.mapper;

import com.om.smartpost.office.staff.dto.request.PostmanCreateRequest;
import com.om.smartpost.office.staff.dto.request.PostmanUpdateRequest;
import com.om.smartpost.office.staff.dto.response.PostmanResponse;
import com.om.smartpost.office.staff.entity.Postman;
import com.om.smartpost.core.identity.entity.User;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class PostmanMapper {

    public User toUser(PostmanCreateRequest request) {
        User user = new User();
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setMobileNo(request.getMobileNo());
        return user;
    }

    public Postman toEntity(PostmanCreateRequest request) {
        Postman postman = new Postman();
        postman.setVehicleNumber(request.getVehicleNumber());
        return postman;
    }

    public void applyToUser(User user, PostmanUpdateRequest request) {
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getMobileNo() != null) {
            user.setMobileNo(request.getMobileNo());
        }
        if (request.getActive() != null) {
            user.setIsActive(request.getActive());
        }
    }

    public void applyToEntity(Postman postman, PostmanUpdateRequest request) {
        if (request.getEmployeeId() != null) {
            postman.setEmployeeId(request.getEmployeeId());
        }
        if (request.getVehicleNumber() != null) {
            postman.setVehicleNumber(request.getVehicleNumber());
        }
    }

    public PostmanResponse toResponse(Postman postman) {
        return new PostmanResponse(
                postman.getId(),
                postman.getUser().getUserId(),
                postman.getUser().getFullName(),
                postman.getUser().getUsername(),
                postman.getUser().getEmail(),
                postman.getUser().getMobileNo(),
                postman.getUser().getIsActive(),
                postman.getEmployeeId(),
                postman.getVehicleNumber(),
                postman.getOffice().getId(),
                postman.getOffice().getName(),
                postman.getBeats() != null ? postman.getBeats().stream().map(beat -> beat.getId()).toList() : Collections.emptyList(),
                postman.getBeats() != null ? postman.getBeats().stream().map(beat -> beat.getName()).toList() : Collections.emptyList()
        );
    }
}


