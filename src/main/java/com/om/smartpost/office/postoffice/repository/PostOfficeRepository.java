package com.om.smartpost.office.postoffice.repository;

import com.om.smartpost.office.postoffice.entity.PostOffice;
import com.om.smartpost.office.postoffice.enums.BranchType;
import com.om.smartpost.office.postoffice.enums.OfficeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostOfficeRepository extends JpaRepository<PostOffice, UUID> {
    boolean existsByNameAndPincode(String name, String pincode);
    boolean existsByPincodeAndBranchType(String pincode, BranchType branchType);
}

