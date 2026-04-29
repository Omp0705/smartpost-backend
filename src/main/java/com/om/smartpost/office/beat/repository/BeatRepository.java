package com.om.smartpost.office.beat.repository;

import com.om.smartpost.office.beat.entity.Beat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BeatRepository extends JpaRepository<Beat, UUID> {
    List<Beat> findByOffice_Id(UUID officeId);
    List<Beat> findByAssignedPostman_Id(UUID postmanId);
    java.util.Optional<Beat> findByBeatCodeAndOffice_Id(String beatCode, UUID officeId);
    boolean existsByBeatCodeAndOffice_Id(String beatCode, UUID officeId);
    long countByOffice_Id(UUID officeId);
    long countByAssignedPostman_Id(UUID postmanId);
}

