package com.om.smartpost.profile.repository;

import com.om.smartpost.profile.entity.SavedAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavedAddressRepository extends JpaRepository<SavedAddress, UUID> {

    List<SavedAddress> findByUser_UserId(Long userId);

}

