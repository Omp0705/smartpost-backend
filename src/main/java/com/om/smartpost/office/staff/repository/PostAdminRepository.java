package com.om.smartpost.office.staff.repository;

import com.om.smartpost.office.staff.entity.PostAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PostAdminRepository extends JpaRepository<PostAdmin, UUID> {
    Optional<PostAdmin> findByUser_UserId(Long userId);
    Optional<PostAdmin> findByEmployeeId(String employeeId);
    boolean existsByEmployeeId(String employeeId);
    long countByOffice_Id(UUID officeId);
}

