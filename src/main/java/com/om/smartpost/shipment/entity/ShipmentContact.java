package com.om.smartpost.shipment.entity;

import com.om.smartpost.shipment.enums.Title;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ShipmentContact {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private Title title;
    
    @Column(nullable = false, name= "firstName")
    private String fname;
    @Column(name = "middleName")
    private String mName;
    @Column(nullable = false, name = "lastName")
    private String lName;

    @Column(nullable = false, name = "mobileNo", length = 15)
    private String mobileNo;

    private String email;
    private String companyName;

    @Column(nullable = false, name = "addressLine1")
    private String addressLine1;
    @Column(name = "addressLine2", length = 255)
    private String addressLine2;
    private String landmark;

    @Column(nullable = false, name = "pincode", length = 6)
    private String pincode;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(length = 20)
    private String dac;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;
    


}



