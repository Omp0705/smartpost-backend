package com.om.smartpost.office.staff.mapper;

import com.om.smartpost.office.staff.dto.request.PostAdminCreateRequest;
import com.om.smartpost.office.staff.dto.request.PostAdminUpdateRequest;
import com.om.smartpost.office.staff.dto.response.PostAdminResponse;
import com.om.smartpost.office.staff.entity.PostAdmin;
import com.om.smartpost.core.identity.entity.User;
import org.springframework.stereotype.Component;

@Component
public class PostAdminMapper {

    public User toUser(PostAdminCreateRequest request) {
        User user = new User();
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setMobileNo(request.getMobileNo());
        return user;
    }

    public PostAdmin toEntity(PostAdminCreateRequest request) {
        PostAdmin postAdmin = new PostAdmin();
        postAdmin.setDesignation(request.getDesignation());
        return postAdmin;
    }

    public void applyToUser(User user, PostAdminUpdateRequest request) {
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

    public void applyToEntity(PostAdmin postAdmin, PostAdminUpdateRequest request) {
        if (request.getEmployeeId() != null) {
            postAdmin.setEmployeeId(request.getEmployeeId());
        }
        if (request.getDesignation() != null) {
            postAdmin.setDesignation(request.getDesignation());
        }
    }

    public PostAdminResponse toResponse(PostAdmin postAdmin) {
        boolean isDelivery = postAdmin.getOffice().getDeliveryStatus() != null &&
                postAdmin.getOffice().getDeliveryStatus().name().equalsIgnoreCase("DELIVERY");
        return new PostAdminResponse(
                postAdmin.getId(),
                postAdmin.getUser().getUserId(),
                postAdmin.getUser().getFullName(),
                postAdmin.getUser().getUsername(),
                postAdmin.getUser().getEmail(),
                postAdmin.getUser().getMobileNo(),
                postAdmin.getUser().getIsActive(),
                postAdmin.getEmployeeId(),
                postAdmin.getDesignation(),
                postAdmin.getOffice().getId(),
                postAdmin.getOffice().getName(),
                postAdmin.getOffice().getPincode(),
                isDelivery
        );
    }
}

