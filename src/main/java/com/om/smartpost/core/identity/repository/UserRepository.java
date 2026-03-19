package com.om.smartpost.core.identity.repository;

import com.om.smartpost.core.identity.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import com.om.smartpost.core.identity.UserRole;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByMobileNo(String mobileNo);


    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    boolean existsByMobileNo(String mobileNo);
    boolean existsByRole(UserRole role);
    Optional<User> findByMobileNoOrEmail(String mobileNo, String email);
}

