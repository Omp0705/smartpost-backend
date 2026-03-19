package com.om.smartpost.profile.entity;

import com.om.smartpost.core.identity.entity.User;

import com.om.smartpost.shipment.enums.DeliverySlot;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "delivery_preferences")
@Data
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    private DeliverySlot preferedDeliverySlot;

 /*  We can add multiple preferences here as needed
    for now we consider this much only
 * */
    private boolean leaveAtDoor;
    private boolean leaveWithGuard;
    private boolean deliverToNeighbor;
    private boolean callBeforeDelivery;
    private boolean otpRequired;
    private boolean signatureRequired;
    private boolean avoidMorning;
    private boolean weekendOnly;

    private String deliveryNote;

}





