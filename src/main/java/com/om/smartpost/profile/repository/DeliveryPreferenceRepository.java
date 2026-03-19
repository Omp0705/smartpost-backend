package com.om.smartpost.profile.repository;

import com.om.smartpost.profile.entity.DeliveryPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryPreferenceRepository
        extends JpaRepository<DeliveryPreference, Long> {

    Optional<DeliveryPreference> findByUser_UserId(Long userId);

}

