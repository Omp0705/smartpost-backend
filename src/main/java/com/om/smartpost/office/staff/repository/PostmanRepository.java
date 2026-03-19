package com.om.smartpost.office.staff.repository;

import com.om.smartpost.office.staff.entity.Postman;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostmanRepository extends JpaRepository<Postman, UUID> {
    // Find a postman profile using their base User ID
    Optional<Postman> findByUser_UserId(Long userId);

    // Find a postman by their employee badge number
    Optional<Postman> findByEmployeeId(String employeeId);
    boolean existsByEmployeeId(String employeeId);
    List<Postman> findByOffice_Id(UUID officeId);
    long countByOffice_Id(UUID officeId);

//    for dashboard stats
// Count active postmen in a specific office

    // Count ALL active postmen (for SUPERADMIN)
}

