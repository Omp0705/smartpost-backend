package com.om.smartpost.office.postoffice.service;

import com.om.smartpost.office.postoffice.dto.request.PostOfficeRequest;
import com.om.smartpost.office.postoffice.dto.response.PostOfficeResponse;
import com.om.smartpost.office.postoffice.entity.PostOffice;
import com.om.smartpost.office.postoffice.enums.BranchType;
import com.om.smartpost.office.postoffice.enums.OfficeType;
import com.om.smartpost.core.error.ErrorCodes;
import com.om.smartpost.core.exception.ApiException;
import com.om.smartpost.office.postoffice.mapper.PostOfficeMapper;
import com.om.smartpost.office.beat.repository.BeatRepository;
import com.om.smartpost.office.staff.repository.PostAdminRepository;
import com.om.smartpost.office.postoffice.repository.PostOfficeRepository;
import com.om.smartpost.office.staff.repository.PostmanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostOfficeService {

    private final PostOfficeRepository postOfficeRepository;
    private final PostAdminRepository postAdminRepository;
    private final PostmanRepository postmanRepository;
    private final BeatRepository beatRepository;
    private final PostOfficeMapper postOfficeMapper;

    @Transactional
    public PostOfficeResponse create(PostOfficeRequest request) {

        BranchType branchType = parseBranchType(request.getBranchType());
        OfficeType deliveryStatus = parseDeliveryStatus(request.getDeliveryStatus());

        if (postOfficeRepository.existsByNameAndPincode(request.getName(), request.getPincode())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    ErrorCodes.DUPLICATE_RESOURCE.toString(),
                    "Post office already exists for this pincode"
            );
        }

        if (branchType == BranchType.HEAD_POST_OFFICE &&
                postOfficeRepository.existsByPincodeAndBranchType(
                        request.getPincode(),
                        BranchType.HEAD_POST_OFFICE)) {

            throw new ApiException(
                    HttpStatus.CONFLICT,
                    ErrorCodes.DUPLICATE_RESOURCE.toString(),
                    "Head Post Office already exists for this pincode"
            );
        }

        PostOffice office = postOfficeMapper.toEntity(request);

        office.setBranchType(branchType);
        office.setDeliveryStatus(deliveryStatus);

        PostOffice saved = postOfficeRepository.save(office);

        return postOfficeMapper.toResponse(saved);
    }


    public List<PostOfficeResponse> getAll() {
        return postOfficeRepository.findAll()
                .stream()
                .map(postOfficeMapper::toResponse)
                .toList();
    }

    public PostOfficeResponse getById(UUID id) {
        return postOfficeMapper.toResponse(findOffice(id));
    }

    @Transactional
    public PostOfficeResponse update(UUID id, PostOfficeRequest request) {
        PostOffice office = findOffice(id);
        postOfficeMapper.apply(office, request);
        return postOfficeMapper.toResponse(postOfficeRepository.save(office));
    }

    @Transactional
    public void delete(UUID id) {
        PostOffice office = findOffice(id);
        if (postAdminRepository.countByOffice_Id(id) > 0
                || postmanRepository.countByOffice_Id(id) > 0
                || beatRepository.countByOffice_Id(id) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_INPUT.toString(), "Delete staff and beats before removing the post office");
        }
        postOfficeRepository.delete(office);
    }

    public PostOffice findOffice(UUID id) {
        return postOfficeRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND.toString(), "Post office not found"));
    }


    private BranchType parseBranchType(String value) {
        try {
            String normalized = value.trim()
                    .replace(" ", "_")
                    .toUpperCase();

            return BranchType.valueOf(normalized);

        } catch (Exception ex) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    ErrorCodes.INVALID_INPUT.toString(),
                    "Invalid branch type: " + value
            );
        }
    }

    private OfficeType parseDeliveryStatus(String value) {
        try {
            String normalized = value.trim()
                    .replace("-", "_")
                    .replace(" ", "_")
                    .toUpperCase();

            return OfficeType.valueOf(normalized);

        } catch (Exception ex) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    ErrorCodes.INVALID_INPUT.toString(),
                    "Invalid delivery status: " + value
            );
        }
    }
}


