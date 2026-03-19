package com.om.smartpost.office.dashboard.service;

import com.om.smartpost.office.dashboard.dto.response.DashboardStatsResponse;
import com.om.smartpost.office.staff.entity.PostAdmin;
import com.om.smartpost.shipment.enums.ShipmentStatus;
import com.om.smartpost.core.identity.UserRole;
import com.om.smartpost.core.exception.ApiException;
import com.om.smartpost.core.error.ErrorCodes;
import com.om.smartpost.office.staff.repository.PostAdminRepository;
import com.om.smartpost.office.staff.repository.PostmanRepository;
import com.om.smartpost.shipment.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ShipmentRepository shipmentRepository;
    private final PostmanRepository postmanRepository;
    private final PostAdminRepository postAdminRepository;

    public DashboardStatsResponse getStats(Long userId, UserRole role) {

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        // If SUPERADMIN, use the global derived queries
        if (role == UserRole.SUPERADMIN) {
            return new DashboardStatsResponse(
                    shipmentRepository.countByCreatedAtBetween(startOfDay, endOfDay),
                    shipmentRepository.countByCurrentStatus(ShipmentStatus.ARRIVED_AT_DESTINATION),
                    shipmentRepository.countByCurrentStatus(ShipmentStatus.OUT_FOR_DELIVERY),
                    shipmentRepository.countByCurrentStatusAndUpdatedAtBetween(ShipmentStatus.DELIVERED, startOfDay, endOfDay),
                    postmanRepository.count()
            );
        }

        // If POSTADMIN,
        if (role == UserRole.POSTADMIN) {
            PostAdmin admin = postAdminRepository.findByUser_UserId(userId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND.toString(), "Admin profile not found"));

            String officeName = admin.getOffice().getName();
            UUID officeId = admin.getOffice().getId();

            return new DashboardStatsResponse(
                    shipmentRepository.countByDestinationPoNameAndCreatedAtBetween(officeName, startOfDay, endOfDay),
                    shipmentRepository.countByDestinationPoNameAndCurrentStatus(officeName, ShipmentStatus.ARRIVED_AT_DESTINATION),
                    shipmentRepository.countByDestinationPoNameAndCurrentStatus(officeName, ShipmentStatus.OUT_FOR_DELIVERY),
                    shipmentRepository.countByDestinationPoNameAndCurrentStatusAndUpdatedAtBetween(officeName, ShipmentStatus.DELIVERED, startOfDay, endOfDay),
                    postmanRepository.countByOffice_Id(officeId) // From our previous fix!
            );
        }

        throw new ApiException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN.toString(), "Access Denied");
    }
}

