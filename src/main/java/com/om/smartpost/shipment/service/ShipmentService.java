package com.om.smartpost.shipment.service;

import com.om.smartpost.core.location.GeocodingService;
import com.om.smartpost.shipment.dto.request.ShipmentContactRequest;
import com.om.smartpost.shipment.dto.request.ShipmentCreateRequest;
import com.om.smartpost.shipment.dto.request.ShipmentDataRequest;
import com.om.smartpost.shipment.dto.response.ShipmentResponse;
import com.om.smartpost.core.identity.entity.User;
import com.om.smartpost.core.identity.repository.UserRepository;
import com.om.smartpost.core.notification.MagicLinkService;
import com.om.smartpost.core.notification.SmsService;
import com.om.smartpost.office.beat.entity.Beat;
import com.om.smartpost.office.beat.repository.BeatRepository;
import com.om.smartpost.office.staff.entity.PostAdmin;
import com.om.smartpost.office.staff.entity.Postman;
import com.om.smartpost.office.staff.repository.PostAdminRepository;
import com.om.smartpost.office.staff.repository.PostmanRepository;
import com.om.smartpost.shipment.entity.Shipment;
import com.om.smartpost.shipment.entity.ShipmentContact;
import com.om.smartpost.shipment.enums.DeliverySlot;
import com.om.smartpost.shipment.enums.ServiceType;
import com.om.smartpost.shipment.enums.ShipmentStatus;
import com.om.smartpost.core.error.ErrorCodes;
import com.om.smartpost.core.exception.ApiException;
import com.om.smartpost.shipment.mapper.ShipmentContactMapper;
import com.om.smartpost.shipment.mapper.ShipmentMapper;
import com.om.smartpost.shipment.prediction.GeocodingResult;
import com.om.smartpost.shipment.repository.ShipmentRepository;
import com.om.smartpost.shipment.events.ShipmentCreatedEvent;
import com.om.smartpost.shipment.prediction.PredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final PostmanRepository postmanRepository;
    private final UserRepository userRepository;
    private final PredictionService predictionService;
    private final ShipmentMapper shipmentMapper;
    private final ShipmentContactMapper shipmentContactMapper;
    private final SmsService smsService;
    private final MagicLinkService magicLinkService;

    private final PostAdminRepository postAdminRepository;
    private final BeatRepository beatRepository;
    private final GeocodingService geocodingService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    public ShipmentResponse createShipment(User sender, ShipmentCreateRequest request) {
        validateServiceMode(request.shipmentData());

        Shipment shipment = shipmentMapper.toEntity(request.shipmentData());
        String shipmentKey = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        shipment.setTrackingNumber("SP-" + shipmentKey);
        shipment.setArticleBarcode("ART-" + shipmentKey);

        shipment.setOriginPincode(request.shipmentData().originPincode());
        shipment.setOriginPoName(request.shipmentData().originPoName());
        shipment.setDestinationPincode(request.shipmentData().destinationPincode());
        shipment.setDestinationPoName(request.shipmentData().destinationPoName());
        shipment.setSenderUser(sender);

        ShipmentContact senderEntity = shipmentContactMapper.toEntity(request.senderDetails());
        ShipmentContact receiverEntity = shipmentContactMapper.toEntity(request.receiverDetails());

        // --- NEW: BACKEND GEOCODING LOGIC ---

        // A. For Sender: Map already provided lat/lng from frontend (Assuming DTO has it)
        if (request.senderDetails().latitude() != null && request.senderDetails().longitude() != null) {
            senderEntity.setLatitude(request.senderDetails().latitude());
            senderEntity.setLongitude(request.senderDetails().longitude());
        }

        String fullReceiverAddress = String.join(", ",
                receiverEntity.getAddressLine1(),
                receiverEntity.getAddressLine2() != null ? receiverEntity.getAddressLine2() : "",
                receiverEntity.getCity(),
                receiverEntity.getState(),
                receiverEntity.getPincode()
        ).replaceAll(" ,", ",");

        try {
            GeocodingResult geoResult = geocodingService.geocodeAddress(fullReceiverAddress, receiverEntity.getPincode());

            if (geoResult.getLatitude() != null && geoResult.getLongitude() != null) {
                receiverEntity.setLatitude(geoResult.getLatitude());
                receiverEntity.setLongitude(geoResult.getLongitude());
                log.info("Successfully fetched receiver coordinates for Tracking: {}", shipment.getTrackingNumber());
            }
        } catch (Exception e) {
            // If Google is down, the shipment still creates successfully, just without coordinates.
            log.warn("Failed to fetch coordinates for receiver. Error: {}", e.getMessage());
        }
        shipment.setSenderDetails(senderEntity);
        shipment.setReceiverDetails(receiverEntity);
        shipment.setReceiverUser(resolveReceiver(request.receiverDetails()));
        shipment.setBookingDate(LocalDate.now());

        predictionService.populatePredictionDetails(shipment);

        shipment.addTrackingEvent(
                ShipmentStatus.CREATED,
                "Shipment data received",
                "SmartPost Hub"
        );
        Shipment savedShipment = shipmentRepository.saveAndFlush(shipment);
//        String token = magicLinkService.generateTrackingToken(savedShipment.getTrackingNumber());
//
//        smsService.sendTrackingMagicLink(
//                request.receiverDetails().mobileNo(),
//                savedShipment.getTrackingNumber(),
//                token
//        );
        eventPublisher.publishEvent(new ShipmentCreatedEvent(savedShipment));
        return shipmentMapper.toResponse(savedShipment);
    }

    public ShipmentResponse getShipmentByTrackingNumber(String trackingNumber) {
        return shipmentMapper.toResponse(findShipmentByTrackingNumber(trackingNumber));
    }

    public List<ShipmentResponse> getMyShipments(Long senderId) {
        return shipmentRepository.findBySenderUser_UserId(senderId)
                .stream()
                .map(shipmentMapper::toResponse)
                .toList();
    }

    @Transactional
    public ShipmentResponse assignPostman(String trackingNumber, String employeeId) {
        Shipment shipment = findShipmentByTrackingNumber(trackingNumber);
        Postman assignedPostman = postmanRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND.toString(), "Postman not found"));

        shipment.setPostman(assignedPostman);
        shipment.addTrackingEvent(
                ShipmentStatus.OUT_FOR_DELIVERY,
                "Package is out for delivery with agent: " + assignedPostman.getUser().getFullName(),
                "Local Sorting Center"
        );

        return shipmentMapper.toResponse(shipmentRepository.saveAndFlush(shipment));
    }

    public List<ShipmentResponse> getIncomingShipments(String receiverMobileNo) {
        // 1. Fetch the raw entity data from the database using the bridge (mobile number)
        List<Shipment> incomingShipments = shipmentRepository.findByReceiverDetails_MobileNo(receiverMobileNo);

        // 2. Map the entities to your DTOs so you don't expose database entities to the frontend
        return incomingShipments.stream()
                .map(shipmentMapper::toResponse) // Assuming you have a helper method to convert Shipment to ShipmentResponse
                .collect(Collectors.toList());
    }

//    all shipments for a user
    public List<ShipmentResponse> getAllMyShipments(Long userId, String userMobile, String userEmail) {

        // Fetch everything linked to this user in one go
        List<Shipment> allShipments = shipmentRepository.findAllUserShipments(userId, userMobile, userEmail);

        return allShipments.stream()
                .map(shipmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ShipmentResponse updateShipmentStatus(String trackingNumber, ShipmentStatus newStatus, String description, String location, Long adminUserId) {
        Shipment shipment = findShipmentByTrackingNumber(trackingNumber);

        PostAdmin admin = postAdminRepository.findByUser_UserId(adminUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Admin profile missing"));

        // 1. State Machine Rule Check
        validateStatusTransition(shipment.getCurrentStatus(), newStatus);

        // 2. Security: Ensure proper office handles the status
        if (newStatus == ShipmentStatus.ARRIVED_AT_DESTINATION || newStatus == ShipmentStatus.OUT_FOR_DELIVERY) {

            if (!shipment.getDestinationPincode().equals(admin.getOffice().getPincode())) {
                throw new ApiException(HttpStatus.FORBIDDEN, "AUTH_ERROR",
                        "Operation Denied: Only the Destination Office (Pincode: " + shipment.getDestinationPincode() + ") can perform this status update.");
            }
        }

        // SPECIAL CASE: Compare PINCODES for Deliveries and Returns too!
        if (newStatus == ShipmentStatus.DELIVERED) {
            if (shipment.getCurrentStatus() == ShipmentStatus.RETURN_TO_SENDER) {
                if (!shipment.getOriginPincode().equals(admin.getOffice().getPincode())) {
                    throw new ApiException(HttpStatus.FORBIDDEN, "AUTH_ERROR",
                            "Operation Denied: Only the Origin Office can mark a returned parcel as Delivered back to the Sender.");
                }
            } else {
                if (!shipment.getDestinationPincode().equals(admin.getOffice().getPincode())) {
                    throw new ApiException(HttpStatus.FORBIDDEN, "AUTH_ERROR",
                            "Operation Denied: Only the Destination Office can deliver this parcel.");
                }
            }
        }

        // 3. Perform Status Update
        shipment.addTrackingEvent(newStatus, description, location);

        // 4. Intelligent Routing Trigger
        if (newStatus == ShipmentStatus.ARRIVED_AT_DESTINATION) {
            autoAssignToBeat(shipment, admin.getOffice().getId(), admin.getOffice().getName());
        }

        return shipmentMapper.toResponse(shipmentRepository.saveAndFlush(shipment));
    }
    @Transactional
    public ShipmentResponse updatePreferredSlot(String trackingNumber, String newSlot) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Shipment not found"));

        if (shipment.getCurrentStatus() == ShipmentStatus.OUT_FOR_DELIVERY ||
                shipment.getCurrentStatus() == ShipmentStatus.DELIVERED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_STATE", "Cannot change slot now");
        }

        try {
            shipment.setPreferredSlot(DeliverySlot.valueOf(newSlot.toUpperCase()));
            shipment.addTrackingEvent(
                    shipment.getCurrentStatus(),
                    "Delivery slot updated to " + newSlot.toLowerCase(),
                    "User Action"
            );
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "Invalid slot name");
        }

        return shipmentMapper.toResponse(shipmentRepository.save(shipment));
    }

    public List<ShipmentResponse> getShipmentsForOffice(String officePincode) {
        List<ShipmentStatus> destinationVisibleStatuses = List.of(
                ShipmentStatus.ACCEPTED,
                ShipmentStatus.ARRIVED_AT_DESTINATION,
                ShipmentStatus.IN_TRANSIT,
                ShipmentStatus.OUT_FOR_DELIVERY,
                ShipmentStatus.DELIVERED,
                ShipmentStatus.DELIVERY_FAILED,
                ShipmentStatus.RETURN_TO_SENDER
        );

        return shipmentRepository.findShipmentsRelevantToOffice(officePincode, destinationVisibleStatuses)
                .stream()
                .map(shipmentMapper::toResponse)
                .toList();
    }

    // --- HELPER METHODS ---

    private Shipment findShipmentByTrackingNumber(String trackingNumber) {
        return shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND.toString(), "Shipment not found"));
    }

    private void validateServiceMode(ShipmentDataRequest request) {
        if (request.serviceType() == ServiceType.PICKUP && request.pickupAddress() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_INPUT.toString(), "Pickup address is required for pickup service");
        }
        if (request.serviceType() == ServiceType.DROP_OFF && request.dropoffInfo() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_INPUT.toString(), "Dropoff info is required for drop-off service");
        }
    }

    private User resolveReceiver(ShipmentContactRequest receiverDetails) {
        if (receiverDetails == null) return null;

        // This handles both checks in a single DB trip
        return userRepository.findByMobileNoOrEmail(
                receiverDetails.mobileNo(),
                receiverDetails.email()
        ).orElse(null);
    }

    private void validateStatusTransition(ShipmentStatus current, ShipmentStatus next) {
        if (current == next) return;

        // Terminal states - once delivered or cancelled, the flow stops entirely.
        if (current == ShipmentStatus.DELIVERED || current == ShipmentStatus.CANCELLED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_TRANSITION",
                    "Shipment is already " + current + " and cannot be updated further.");
        }

        boolean isValid = switch (next) {
            case ACCEPTED -> current == ShipmentStatus.PENDING || current == ShipmentStatus.CREATED;
            case IN_TRANSIT -> current == ShipmentStatus.ACCEPTED || current == ShipmentStatus.CREATED || current == ShipmentStatus.RETURN_TO_SENDER;
            case ARRIVED_AT_DESTINATION -> current == ShipmentStatus.IN_TRANSIT || current == ShipmentStatus.ACCEPTED || current == ShipmentStatus.RETURN_TO_SENDER;
            case OUT_FOR_DELIVERY -> current == ShipmentStatus.ARRIVED_AT_DESTINATION || current == ShipmentStatus.DELIVERY_FAILED;
            case DELIVERY_FAILED -> current == ShipmentStatus.OUT_FOR_DELIVERY;
            case DELIVERED -> current == ShipmentStatus.OUT_FOR_DELIVERY || current == ShipmentStatus.RETURN_TO_SENDER;
            case RETURN_TO_SENDER -> true; // A return can be triggered at almost any stage (e.g. customer refused at door, address wrong at hub)
            case CANCELLED -> current == ShipmentStatus.PENDING || current == ShipmentStatus.CREATED || current == ShipmentStatus.ACCEPTED;
            default -> true;
        };

        if (!isValid) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_TRANSITION",
                    "Logical Error: Cannot transition shipment directly from " + current + " to " + next);
        }
    }

    private void autoAssignToBeat(Shipment shipment, UUID destinationOfficeId, String destinationOfficeName) {
        List<Beat> localBeats = beatRepository.findByOffice_Id(destinationOfficeId);

        String address1 = shipment.getReceiverDetails().getAddressLine1() != null ? shipment.getReceiverDetails().getAddressLine1() : "";
        String address2 = shipment.getReceiverDetails().getAddressLine2() != null ? shipment.getReceiverDetails().getAddressLine2() : "";
        String landmark = shipment.getReceiverDetails().getLandmark() != null ? shipment.getReceiverDetails().getLandmark() : "";

        String fullAddress = (address1 + " " + address2 + " " + landmark).toLowerCase();

        for (Beat beat : localBeats) {
            if (beat.getAreaKeywords() == null || beat.getAreaKeywords().isEmpty()) continue;

            String[] keywords = beat.getAreaKeywords().split(",");
            for (String keyword : keywords) {
                String cleanKey = keyword.trim().toLowerCase();

                if (!cleanKey.isEmpty() && fullAddress.contains(cleanKey)) {
                    if (beat.getAssignedPostman() != null) {
                        shipment.setPostman(beat.getAssignedPostman());

                        shipment.addTrackingEvent(
                                ShipmentStatus.ARRIVED_AT_DESTINATION,
                                "Parcel sorted via SmartRouting to " + beat.getName() + " (Assigned: " + beat.getAssignedPostman().getUser().getFullName() + ")",
                                destinationOfficeName
                        );
                        log.info("Successfully Auto-Assigned {} to Beat: {}", shipment.getTrackingNumber(), beat.getName());
                        return;
                    }
                }
            }
        }
        log.warn("Auto-Assignment Failed: No matching beat keywords found for Address [{}]", fullAddress);
    }
}



