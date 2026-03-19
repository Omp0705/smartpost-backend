package com.om.smartpost.shipment.repository;

import com.om.smartpost.shipment.entity.Shipment;
import com.om.smartpost.core.identity.entity.User;
import com.om.smartpost.shipment.enums.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    List<Shipment> findBySenderUser(User sender);

    // Fetch all shipments sent by a specific user (Guest or Registered)
    List<Shipment> findBySenderUser_UserId(Long userId);
    // Finds all shipments where the provided phone number matches the receiver's phone number
    List<Shipment> findByReceiverDetails_MobileNo(String mobileNo);

    long countByReceiverDetails_MobileNoAndCurrentStatus(String mobileNo, com.om.smartpost.shipment.enums.ShipmentStatus status);

//    @Query("SELECT s FROM Shipment s WHERE " +
//            "(s.originPincode = :pincode AND s.originPoName = :poName) " +
//            "OR " +
//            "(s.destinationPincode = :pincode AND s.destinationPoName = :poName AND s.currentStatus != 'CREATED') " +
//            "ORDER BY s.createdAt DESC")
//    List<Shipment> findShipmentsForOffice(@Param("pincode") String pincode, @Param("poName") String poName);

    @Query("""
        SELECT s FROM Shipment s 
        WHERE s.originPincode = :pincode 
           OR (s.destinationPincode = :pincode AND s.currentStatus IN :destinationVisibleStatuses) 
        ORDER BY s.createdAt DESC
    """)
    List<Shipment> findShipmentsRelevantToOffice(
            @Param("pincode") String pincode,
            @Param("destinationVisibleStatuses") List<ShipmentStatus> destinationVisibleStatuses
    );

    @Query("""
        SELECT s FROM Shipment s 
        WHERE s.senderUser.userId = :userId 
           OR s.receiverUser.userId = :userId
           OR s.receiverDetails.mobileNo = :phone 
           OR s.receiverDetails.email = :email
    """)
    List<Shipment> findAllUserShipments(
            @Param("userId") Long userId,
            @Param("phone") String phone,
            @Param("email") String email
    );

//    dashboard stats
long countByCreatedAtBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);
    long countByCurrentStatus(ShipmentStatus status);
    long countByCurrentStatusAndUpdatedAtBetween(ShipmentStatus status, LocalDateTime startOfDay, LocalDateTime endOfDay);

    // --- POSTADMIN Branch-Specific Queries ---
    long countByDestinationPoNameAndCreatedAtBetween(String officeName, LocalDateTime startOfDay, LocalDateTime endOfDay);
    long countByDestinationPoNameAndCurrentStatus(String officeName, ShipmentStatus status);
    long countByDestinationPoNameAndCurrentStatusAndUpdatedAtBetween(String officeName, ShipmentStatus status, LocalDateTime startOfDay, LocalDateTime endOfDay);
}

