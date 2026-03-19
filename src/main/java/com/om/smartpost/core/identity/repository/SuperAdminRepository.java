package com.om.smartpost.core.identity.repository;

import com.om.smartpost.core.identity.entity.SuperAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SuperAdminRepository extends JpaRepository<SuperAdmin, UUID> {
    Optional<SuperAdmin> findByUser_UserId(Long userId);
    boolean existsByUser_UserId(Long userId);
    long countBy();
}

