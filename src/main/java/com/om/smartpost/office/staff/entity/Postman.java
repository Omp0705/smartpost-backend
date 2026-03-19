package com.om.smartpost.office.staff.entity;

import com.om.smartpost.core.identity.entity.User;
import com.om.smartpost.office.beat.entity.Beat;
import com.om.smartpost.office.postoffice.entity.PostOffice;
import com.om.smartpost.shipment.entity.Shipment;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "postmen")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Postman {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // The core link back to the User table for auth, name, and phone number
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    // Employee specific fields
    @Column(name = "employee_id", unique = true, nullable = false)
    private String employeeId;

    @Column(name = "vehicle_number")
    private String vehicleNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "office_id", nullable = false)
    private PostOffice office;

    @OneToMany(mappedBy = "assignedPostman")
    @Builder.Default
    private List<Beat> beats = new ArrayList<>();

    // The One-to-Many link to shipments
    @OneToMany(mappedBy = "postman")
    @Builder.Default
    private List<Shipment> activeShipments = new ArrayList<>();
}





