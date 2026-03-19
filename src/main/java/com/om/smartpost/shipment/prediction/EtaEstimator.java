package com.om.smartpost.shipment.prediction;

import com.om.smartpost.shipment.enums.DeliveryPriority;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class EtaEstimator {

    public LocalDate estimate(LocalDate bookingDate, DeliveryPriority priority, RouteTier routeTier) {
        return bookingDate.plusDays(resolveEtaDays(priority, routeTier));
    }

    long resolveEtaDays(DeliveryPriority priority, RouteTier routeTier) {
        return switch (priority) {
            case EXPRESS -> switch (routeTier) {
                case LOCAL, INTRA_STATE -> 1;
                case INTER_STATE -> 2;
                case REMOTE -> 3;
            };
            case SPEED -> switch (routeTier) {
                case LOCAL -> 1;
                case INTRA_STATE -> 2;
                case INTER_STATE -> 3;
                case REMOTE -> 4;
            };
            case NORMAL -> switch (routeTier) {
                case LOCAL -> 2;
                case INTRA_STATE -> 4;
                case INTER_STATE -> 5;
                case REMOTE -> 7;
            };
        };
    }
}

