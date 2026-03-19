package com.om.smartpost.shipment.entity;

import com.om.smartpost.core.identity.entity.User;
import com.om.smartpost.office.staff.entity.Postman;
import com.om.smartpost.shipment.enums.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "shipments")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, updatable = false)
    private String trackingNumber;

    @Column(unique = true, nullable = false, updatable = false)
    private String articleBarcode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceType serviceType;

    @OneToOne(cascade = CascadeType.ALL,orphanRemoval = true)
    @JoinColumn(name = "pickup_address_id")
    private Address pickupAddress;
    @OneToOne(cascade = CascadeType.ALL,orphanRemoval = true)
    @JoinColumn(name = "dropoff_info_id")
    private DropoffInfo dropoffInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id",nullable = false)
    private User senderUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_user_id")
    private User receiverUser;

    // Sender and Receiver Details Address and Basic Info
    //  Has one to one relationship with the ShipmentContact Tabel 
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_contact_id",referencedColumnName = "id")
    private ShipmentContact senderDetails;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "receiver_contact_id", referencedColumnName = "id")
    private ShipmentContact receiverDetails;

    @Column(nullable = false)
    private String originPincode;

    @Column(nullable = false)
    private String originPoName;

    @Column(nullable = false)
    private String destinationPincode;

    @Column(nullable = false)
    private String destinationPoName;

//    Parcel details
    @Enumerated(EnumType.STRING)
    private AreaType areaType;

    @Enumerated(EnumType.STRING)
    private DeliveryPriority deliveryPriority;

    @Enumerated(EnumType.STRING)
    private ShipmentStatus currentStatus;

    private Double weightKg;
    private boolean fragile;
    private boolean isMerchandise;

    // Physical Dimensions (from your image)
    private Double lengthCms;
    private Double widthCms;
    private Double heightCms;

    // Weights (in grams to match the India Post image standard)
    private Double physicalWeightGms;
    private Double volumetricWeightGms;
    private Double chargedWeightGms;

    @Column(precision = 10, scale = 2)
    private BigDecimal codAmount;

    @Enumerated(EnumType.STRING)
    private DeliverySlot preferredSlot;

    @Enumerated(EnumType.STRING)
    private DeliverySlot predictedSlot;

    @Column(nullable = false)
    private LocalDate bookingDate;

    @Column
    private LocalDate deliveryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postman_id")
    private Postman postman;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // This makes generating your frontend Timeline incredibly easy!
    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("timestamp ASC")
    @Builder.Default
    private List<TrackingEvent> trackingHistory = new ArrayList<>();

    // --- Validations ---

    @AssertTrue(message = "Pickup Address is required for Pickup service")
    public boolean isPickupValid() {
        if (serviceType == ServiceType.PICKUP) return pickupAddress != null && dropoffInfo == null;
        return true;
    }

    @AssertTrue(message = "Dropoff Info is required for Dropoff service")
    public boolean isDropoffValid() {
        if (serviceType == ServiceType.DROP_OFF) return dropoffInfo != null && pickupAddress == null;
        return true;
    }

    // Helper method to add events easily
    public void addTrackingEvent(ShipmentStatus status, String description, String location) {
        TrackingEvent event = TrackingEvent.builder()
                .shipment(this)
                .status(status)
                .description(description)
                .location(location)
                .build();
        this.trackingHistory.add(event);
        this.setCurrentStatus(status);
    }

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


}





