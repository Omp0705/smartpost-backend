package com.om.smartpost.office.beat.service;

import com.om.smartpost.office.beat.dto.request.BeatAssignmentRequest;
import com.om.smartpost.office.beat.dto.request.BeatRequest;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BeatService {

    private final BeatRepository beatRepository;
    private final PostmanRepository postmanRepository;
    private final PostAdminRepository postAdminRepository;
    private final PostOfficeService postOfficeService;
    private final BeatMapper beatMapper;

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
}



