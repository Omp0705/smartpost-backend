package com.om.smartpost.shipment.prediction;

import com.om.smartpost.shipment.enums.DeliveryPriority;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EtaEstimatorTest {

    private final EtaEstimator etaEstimator = new EtaEstimator();

    @Test
    void estimatesExpressLocal() {
        assertEquals(LocalDate.of(2026, 3, 13),
                etaEstimator.estimate(LocalDate.of(2026, 3, 12), DeliveryPriority.EXPRESS, RouteTier.LOCAL));
    }

    @Test
    void estimatesExpressRemote() {
        assertEquals(LocalDate.of(2026, 3, 15),
                etaEstimator.estimate(LocalDate.of(2026, 3, 12), DeliveryPriority.EXPRESS, RouteTier.REMOTE));
    }

    @Test
    void estimatesSpeedInterState() {
        assertEquals(LocalDate.of(2026, 3, 15),
                etaEstimator.estimate(LocalDate.of(2026, 3, 12), DeliveryPriority.SPEED, RouteTier.INTER_STATE));
    }

    @Test
    void estimatesNormalIntraState() {
        assertEquals(LocalDate.of(2026, 3, 16),
                etaEstimator.estimate(LocalDate.of(2026, 3, 12), DeliveryPriority.NORMAL, RouteTier.INTRA_STATE));
    }
}

