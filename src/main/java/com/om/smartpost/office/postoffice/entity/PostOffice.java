package com.om.smartpost.office.postoffice.entity;

import com.om.smartpost.office.beat.entity.Beat;
import com.om.smartpost.office.staff.entity.PostAdmin;
import com.om.smartpost.office.staff.entity.Postman;

import com.om.smartpost.office.postoffice.enums.BranchType;
import com.om.smartpost.office.postoffice.enums.OfficeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "post_offices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostOffice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "branch_type", nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    private BranchType branchType;

    @Column(name = "delivery_status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private OfficeType deliveryStatus;

    @Column(nullable = false, length = 120)
    private String circle;

    @Column(nullable = false, length = 120)
    private String district;

    @Column(nullable = false, length = 120)
    private String division;

    @Column(nullable = false, length = 120)
    private String region;

    @Column(length = 120)
    private String block;

    @Column(nullable = false, length = 120)
    private String state;

    @Column(nullable = false, length = 120)
    private String country;

    @Column(nullable = false, length = 10)
    private String pincode;

    @OneToMany(mappedBy = "office", cascade = CascadeType.ALL)
    @Builder.Default
    private List<PostAdmin> postAdmins = new ArrayList<>();

    @OneToMany(mappedBy = "office", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Postman> postmen = new ArrayList<>();

    @OneToMany(mappedBy = "office", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Beat> beats = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}





