package com.om.smartpost.office.dashboard.dto.response;

public record DashboardStatsResponse(
        long shipmentsToday,
        long pendingShipments,
        long assignedShipments,
        long deliveredShipments,
        long activePostmen
) {
}


