package com.om.smartpost.shipment.entity;

import com.om.smartpost.shipment.enums.AddressType;
import com.om.smartpost.shipment.enums.Title;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Table(name = "addresses")
@Entity
@Getter @Setter
@Builder
@AllArgsConstructor @NoArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Enumerated(EnumType.STRING)
    private AddressType addressType;

//    Personal Details
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Title title;

    @Column(nullable = false, length = 50)
    private String firstName;
    @Column(length = 50)
    private String middleName;
    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(length = 50)
    @Email
    private String email;
    @Column(nullable = false, length = 15)
    private String mobileNo;

//    Address Details
    @Column(length = 100)
    private String companyName;
    @Column(nullable = false,length = 255)
    private String addressLine1;
    @Column(length = 255)
    private String addressLine2;
    @Column(length = 100)
    private String landmark;
    @Column(nullable = false, length = 6)
    @Pattern(regexp = "^\\d{6}$", message = "Pincode must be exactly 6 digits")
    private String pincode;
    @Column(length = 50)
    private String city;
    @Column(length = 50)
    private String state;



}




