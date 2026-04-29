package com.om.smartpost.office.beat.service;

import com.om.smartpost.office.beat.dto.request.BeatAssignmentRequest;
import com.om.smartpost.office.beat.dto.request.BeatRequest;
import com.om.smartpost.office.beat.dto.request.GeneratedBeatSaveItemRequest;
import com.om.smartpost.office.beat.dto.request.GeneratedBeatSaveRequest;
import com.om.smartpost.office.beat.dto.response.BeatResponse;
import com.om.smartpost.office.beat.entity.Beat;
import com.om.smartpost.office.staff.entity.PostAdmin;
import com.om.smartpost.office.postoffice.entity.PostOffice;
import com.om.smartpost.office.staff.entity.Postman;
import com.om.smartpost.core.identity.UserRole;
import com.om.smartpost.core.error.ErrorCodes;
import com.om.smartpost.core.exception.ApiException;
import com.om.smartpost.office.beat.mapper.BeatMapper;
import com.om.smartpost.office.beat.repository.BeatRepository;
import com.om.smartpost.office.postoffice.service.PostOfficeService;
import com.om.smartpost.office.staff.repository.PostAdminRepository;
import com.om.smartpost.office.staff.repository.PostmanRepository;
import com.om.smartpost.shipment.dto.response.ShipmentResponse;
import com.om.smartpost.shipment.entity.Shipment;
import com.om.smartpost.shipment.mapper.ShipmentMapper;
import com.om.smartpost.shipment.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BeatService {

    private final BeatRepository beatRepository;
    private final PostmanRepository postmanRepository;
    private final PostAdminRepository postAdminRepository;
    private final PostOfficeService postOfficeService;
    private final BeatMapper beatMapper;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentMapper shipmentMapper;

    @Transactional
    public BeatResponse create(Long actorUserId, UserRole actorRole, BeatRequest request) {
        validateOfficeAccess(actorUserId, actorRole, request.getOfficeId());

        if (beatRepository.existsByBeatCodeAndOffice_Id(request.getBeatCode(), request.getOfficeId())) {
            throw new ApiException(HttpStatus.CONFLICT, ErrorCodes.DUPLICATE_RESOURCE.toString(), "Beat code already exists for this office");
        }

        PostOffice office = postOfficeService.findOffice(request.getOfficeId());
        Beat beat = beatMapper.toEntity(request);
        beat.setOffice(office);
        return beatMapper.toResponse(beatRepository.save(beat));
    }

    public List<BeatResponse> getAll(Long actorUserId, UserRole actorRole, UUID requestedOfficeId) {

        UUID targetOfficeId = requestedOfficeId;

        // If PostAdmin, FORCE the targetOfficeId to their own office (ignore the requested one)
        if (actorRole == UserRole.POSTADMIN) {
            PostAdmin postAdmin = postAdminRepository.findByUser_UserId(actorUserId)
                    .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN.toString(), "Admin profile not found"));
            targetOfficeId = postAdmin.getOffice().getId();
        }

        List<Beat> beats = targetOfficeId == null ? beatRepository.findAll() : beatRepository.findByOffice_Id(targetOfficeId);
        return beats.stream().map(beatMapper::toResponse).toList();
    }

    public BeatResponse getById(UUID beatId, Long actorUserId, UserRole actorRole) {
        Beat beat = findBeat(beatId);
        validateOfficeAccess(actorUserId, actorRole, beat.getOffice().getId());
        return beatMapper.toResponse(beat);
    }

    @Transactional(readOnly = true)
    public List<BeatResponse> getAssignedBeats(Long actorUserId, UserRole actorRole, UUID requestedPostmanId) {
        UUID targetPostmanId;
        if (actorRole == UserRole.POSTMAN) {
            Postman actorPostman = postmanRepository.findByUser_UserId(actorUserId)
                    .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN.toString(), "Postman profile not found"));

            if (requestedPostmanId != null && !requestedPostmanId.equals(actorPostman.getId())) {
                throw new ApiException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN.toString(), "You can view only your assigned beats");
            }
            targetPostmanId = actorPostman.getId();
        } else if (actorRole == UserRole.POSTADMIN || actorRole == UserRole.SUPERADMIN) {
            if (requestedPostmanId == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_INPUT.toString(), "Postman ID is required");
            }

            Postman targetPostman = postmanRepository.findById(requestedPostmanId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND.toString(), "Postman not found"));
            validateOfficeAccess(actorUserId, actorRole, targetPostman.getOffice().getId());
            targetPostmanId = targetPostman.getId();
        } else {
            throw new ApiException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN.toString(), "You do not have permission to access this resource");
        }

        return beatRepository.findByAssignedPostman_Id(targetPostmanId)
                .stream()
                .map(beatMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ShipmentResponse> getShipmentsByBeatId(UUID beatId, Long actorUserId, UserRole actorRole) {
        Beat beat = findBeat(beatId);
        validateBeatShipmentAccess(beat, actorUserId, actorRole);

        return shipmentRepository.findByBeat_IdOrderByCreatedAtDesc(beatId)
                .stream()
                .map(shipmentMapper::toResponse)
                .toList();
    }

    @Transactional
    public BeatResponse update(UUID beatId, Long actorUserId, UserRole actorRole, BeatRequest request) {
        Beat beat = findBeat(beatId);
        validateOfficeAccess(actorUserId, actorRole, beat.getOffice().getId());

        if (!beat.getOffice().getId().equals(request.getOfficeId())) {
            validateOfficeAccess(actorUserId, actorRole, request.getOfficeId());
            beat.setOffice(postOfficeService.findOffice(request.getOfficeId()));
        }

        beatMapper.apply(beat, request);
        return beatMapper.toResponse(beatRepository.save(beat));
    }

    @Transactional
    public BeatResponse assignPostman(UUID beatId, Long actorUserId, UserRole actorRole, BeatAssignmentRequest request) {
        Beat beat = findBeat(beatId);
        validateOfficeAccess(actorUserId, actorRole, beat.getOffice().getId());

        if (request.getPostmanId() == null) {
            beat.setAssignedPostman(null);
            return beatMapper.toResponse(beatRepository.save(beat));
        }

        Postman postman = postmanRepository.findById(request.getPostmanId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND.toString(), "Postman not found"));

        if (!postman.getOffice().getId().equals(beat.getOffice().getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_INPUT.toString(), "Beat and postman must belong to the same office");
        }

        beat.setAssignedPostman(postman);
        return beatMapper.toResponse(beatRepository.save(beat));
    }

    @Transactional
    public List<BeatResponse> saveGeneratedBeats(Long actorUserId, UserRole actorRole, GeneratedBeatSaveRequest request) {
        validateOfficeAccess(actorUserId, actorRole, request.getOfficeId());

        PostOffice office = postOfficeService.findOffice(request.getOfficeId());
        validateNoDuplicateShipmentIds(request.getBeats());

        List<BeatResponse> responses = new java.util.ArrayList<>();
        for (GeneratedBeatSaveItemRequest item : request.getBeats()) {
            Postman postman = null;
            if (item.getAssignedPostmanId() != null) {
                postman = postmanRepository.findById(item.getAssignedPostmanId())
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND.toString(), "Postman not found"));

                if (!postman.getOffice().getId().equals(office.getId())) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_INPUT.toString(), "Beat and postman must belong to the same office");
                }
            }

            Beat beat = beatRepository.findByBeatCodeAndOffice_Id(item.getBeatCode(), office.getId())
                    .orElseGet(Beat::new);
            boolean isNew = beat.getId() == null;
            if (isNew) {
                beat.setOffice(office);
            }

            beat.setBeatCode(item.getBeatCode());
            beat.setName(item.getName());
            beat.setDescription(item.getDescription());
            beat.setAreaKeywords(item.getAreaKeywords());
            beat.setRouteOrder(item.getRouteOrder());
            beat.setActive(Boolean.TRUE);
            beat.setAssignedPostman(postman);

            Beat savedBeat = beatRepository.save(beat);
            Set<UUID> shipmentIds = new HashSet<>(item.getShipmentIds());
            detachRemovedShipments(savedBeat, shipmentIds);

            List<Shipment> shipments = shipmentRepository.findAllById(item.getShipmentIds());
            if (shipments.size() != shipmentIds.size()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_INPUT.toString(), "One or more shipments were not found");
            }

            for (Shipment shipment : shipments) {
                if (!shipmentBelongsToOffice(shipment, office)) {
                    continue;
                }
                shipment.setBeat(savedBeat);
                shipment.setPostman(postman);
            }
            shipmentRepository.saveAll(shipments);
            responses.add(beatMapper.toResponse(savedBeat));
        }

        return responses;
    }

    @Transactional
    public void delete(UUID beatId, Long actorUserId, UserRole actorRole) {
        Beat beat = findBeat(beatId);
        validateOfficeAccess(actorUserId, actorRole, beat.getOffice().getId());
        beatRepository.delete(beat);
    }

    public Beat findBeat(UUID beatId) {
        return beatRepository.findById(beatId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND.toString(), "Beat not found"));
    }

    private void validateOfficeAccess(Long actorUserId, UserRole actorRole, UUID officeId) {
        if (actorRole == UserRole.SUPERADMIN) {
            return;
        }
        if (actorRole != UserRole.POSTADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN.toString(), "Only post admins or super admins can manage beats");
        }

        PostAdmin postAdmin = postAdminRepository.findByUser_UserId(actorUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN.toString(), "Post admin profile not found"));

        if (!postAdmin.getOffice().getId().equals(officeId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN.toString(), "You can manage beats only for your assigned office");
        }
    }

    private void validateNoDuplicateShipmentIds(List<GeneratedBeatSaveItemRequest> beats) {
        Set<UUID> seen = new HashSet<>();
        for (GeneratedBeatSaveItemRequest beat : beats) {
            for (UUID shipmentId : beat.getShipmentIds()) {
                if (!seen.add(shipmentId)) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_INPUT.toString(), "A shipment cannot belong to more than one generated beat in the same request");
                }
            }
        }
    }

    private void detachRemovedShipments(Beat beat, Set<UUID> keptShipmentIds) {
        if (beat.getShipments() == null) {
            return;
        }
        for (Shipment existingShipment : beat.getShipments()) {
            if (!keptShipmentIds.contains(existingShipment.getId())) {
                existingShipment.setBeat(null);
                existingShipment.setPostman(null);
            }
        }
    }

    private boolean shipmentBelongsToOffice(Shipment shipment, PostOffice office) {
        return office.getPincode().equals(shipment.getDestinationPincode());
    }

    private void validateBeatShipmentAccess(Beat beat, Long actorUserId, UserRole actorRole) {
        if (actorRole == UserRole.SUPERADMIN) {
            return;
        }

        if (actorRole == UserRole.POSTADMIN) {
            validateOfficeAccess(actorUserId, actorRole, beat.getOffice().getId());
            return;
        }

        if (actorRole == UserRole.POSTMAN) {
            Postman actorPostman = postmanRepository.findByUser_UserId(actorUserId)
                    .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN.toString(), "Postman profile not found"));

            if (beat.getAssignedPostman() == null || !actorPostman.getId().equals(beat.getAssignedPostman().getId())) {
                throw new ApiException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN.toString(), "You can access shipments only for your assigned beats");
            }
            return;
        }

        throw new ApiException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN.toString(), "You do not have permission to access this resource");
    }
}



