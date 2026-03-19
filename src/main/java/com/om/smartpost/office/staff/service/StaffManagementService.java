package com.om.smartpost.office.staff.service;

import com.om.smartpost.office.staff.dto.request.PostAdminCreateRequest;
import com.om.smartpost.office.staff.dto.request.PostAdminUpdateRequest;
import com.om.smartpost.office.staff.dto.request.PostmanCreateRequest;
import com.om.smartpost.office.staff.dto.request.PostmanUpdateRequest;
import com.om.smartpost.office.staff.dto.response.PostAdminProfileResponse;
import com.om.smartpost.office.staff.dto.response.PostAdminResponse;
import com.om.smartpost.office.staff.dto.response.PostmanResponse;
import com.om.smartpost.office.beat.entity.Beat;
import com.om.smartpost.office.staff.entity.PostAdmin;
import com.om.smartpost.office.postoffice.entity.PostOffice;
import com.om.smartpost.office.staff.entity.Postman;
import com.om.smartpost.core.identity.entity.User;
import com.om.smartpost.core.identity.UserRole;
import com.om.smartpost.core.error.ErrorCodes;
import com.om.smartpost.core.exception.ApiException;
import com.om.smartpost.office.postoffice.service.PostOfficeService;
import com.om.smartpost.office.staff.mapper.PostAdminMapper;
import com.om.smartpost.office.staff.mapper.PostmanMapper;
import com.om.smartpost.office.beat.repository.BeatRepository;
import com.om.smartpost.office.staff.repository.PostAdminRepository;
import com.om.smartpost.office.staff.repository.PostmanRepository;
import com.om.smartpost.core.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StaffManagementService {

    private final UserRepository userRepository;
    private final PostmanRepository postmanRepository;
    private final PostAdminRepository postAdminRepository;
    private final BeatRepository beatRepository;
    private final PostOfficeService postOfficeService;
    private final PasswordEncoder passwordEncoder;
    private final PostmanMapper postmanMapper;
    private final PostAdminMapper postAdminMapper;

    @Transactional
    public PostAdminResponse createPostAdmin(PostAdminCreateRequest request) {
        validateUserIdentityUniqueness(null, request.getUsername(), request.getEmail(), request.getMobileNo());

        // REMOVED: validatePostAdminEmployeeId(null, request.getEmployeeId());

        User user = postAdminMapper.toUser(request);
        user.setRole(UserRole.POSTADMIN);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(true);

        PostOffice office = postOfficeService.findOffice(request.getOfficeId());

        PostAdmin postAdmin = postAdminMapper.toEntity(request);

        // GENERATE AND ASSIGN UNIQUE EMPLOYEE ID
        postAdmin.setEmployeeId(generateUniqueAdminEmployeeId());

        postAdmin.setUser(userRepository.save(user));
        postAdmin.setOffice(office);

        return postAdminMapper.toResponse(postAdminRepository.save(postAdmin));
    }

    // Helper method to generate the unique ID
    private String generateUniqueAdminEmployeeId() {
        String empId;
        do {
            // Generates a random 6-digit number between 100000 and 999999
            int randomNum = java.util.concurrent.ThreadLocalRandom.current().nextInt(100000, 1000000);
            empId = "EMP" + randomNum;

            // Keep generating if by some chance this exact ID is already in the database
        } while (postAdminRepository.existsByEmployeeId(empId));

        return empId;
    }

    public List<PostAdminResponse> getPostAdmins() {
        return postAdminRepository.findAll().stream().map(postAdminMapper::toResponse).toList();
    }

    @Transactional
    public PostAdminResponse updatePostAdmin(UUID postAdminId, PostAdminUpdateRequest request) {
        PostAdmin postAdmin = findPostAdmin(postAdminId);
        validateUserIdentityUniqueness(postAdmin.getUser().getUserId(), request.getUsername(), request.getEmail(), request.getMobileNo());
        validatePostAdminEmployeeId(postAdmin.getId(), request.getEmployeeId());

        postAdminMapper.applyToUser(postAdmin.getUser(), request);
        postAdminMapper.applyToEntity(postAdmin, request);

        if (request.getPassword() != null) {
            postAdmin.getUser().setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getOfficeId() != null) {
            postAdmin.setOffice(postOfficeService.findOffice(request.getOfficeId()));
        }

        userRepository.save(postAdmin.getUser());
        return postAdminMapper.toResponse(postAdminRepository.save(postAdmin));
    }

    @Transactional
    public void deletePostAdmin(UUID postAdminId) {
        PostAdmin postAdmin = findPostAdmin(postAdminId);
        postAdminRepository.delete(postAdmin);
        userRepository.delete(postAdmin.getUser());
    }

    @Transactional
    public PostmanResponse createPostman(Long actorUserId, UserRole actorRole, PostmanCreateRequest request) {
        validateOfficeAccess(actorUserId, actorRole, request.getOfficeId());
        validateUserIdentityUniqueness(null, request.getUsername(), request.getEmail(), request.getMobileNo());

        // REMOVED: validatePostmanEmployeeId(null, request.getEmployeeId());

        User user = postmanMapper.toUser(request);
        user.setRole(UserRole.POSTMAN);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(true);

        PostOffice office = postOfficeService.findOffice(request.getOfficeId());

        Postman postman = postmanMapper.toEntity(request);

        // GENERATE AND ASSIGN UNIQUE EMPLOYEE ID
        postman.setEmployeeId(generateUniqueEmployeeId());

        postman.setUser(userRepository.save(user));
        postman.setOffice(office);

        Postman savedPostman = postmanRepository.save(postman);
        syncBeats(savedPostman, request.getBeatIds());

        return postmanMapper.toResponse(postmanRepository.findById(savedPostman.getId()).orElse(savedPostman));
    }

    // Helper method to generate the unique ID
    private String generateUniqueEmployeeId() {
        String empId;
        do {
            // Generates a random 6-digit number between 100000 and 999999
            int randomNum = java.util.concurrent.ThreadLocalRandom.current().nextInt(100000, 1000000);
            empId = "EMP" + randomNum;

            // Keep generating if by some chance this exact ID is already in the database
        } while (postmanRepository.existsByEmployeeId(empId));

        return empId;
    }

    public List<PostmanResponse> getPostmen(UUID officeId) {
        List<Postman> postmen = officeId == null ? postmanRepository.findAll() : postmanRepository.findByOffice_Id(officeId);
        return postmen.stream().map(postmanMapper::toResponse).toList();
    }

    @Transactional
    public PostmanResponse updatePostman(UUID postmanId, Long actorUserId, UserRole actorRole, PostmanUpdateRequest request) {
        Postman postman = findPostman(postmanId);
        validateOfficeAccess(actorUserId, actorRole, postman.getOffice().getId());
        validateUserIdentityUniqueness(postman.getUser().getUserId(), request.getUsername(), request.getEmail(), request.getMobileNo());
        validatePostmanEmployeeId(postman.getId(), request.getEmployeeId());

        postmanMapper.applyToUser(postman.getUser(), request);
        postmanMapper.applyToEntity(postman, request);

        if (request.getPassword() != null) {
            postman.getUser().setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getOfficeId() != null && !request.getOfficeId().equals(postman.getOffice().getId())) {
            validateOfficeAccess(actorUserId, actorRole, request.getOfficeId());
            postman.setOffice(postOfficeService.findOffice(request.getOfficeId()));
        }

        userRepository.save(postman.getUser());
        Postman savedPostman = postmanRepository.save(postman);
        if (request.getBeatIds() != null) {
            syncBeats(savedPostman, request.getBeatIds());
        }
        return postmanMapper.toResponse(postmanRepository.findById(savedPostman.getId()).orElse(savedPostman));
    }

    @Transactional
    public void deletePostman(UUID postmanId, Long actorUserId, UserRole actorRole) {
        Postman postman = findPostman(postmanId);
        validateOfficeAccess(actorUserId, actorRole, postman.getOffice().getId());

        List<Beat> assignedBeats = new ArrayList<>(postman.getBeats());
        for (Beat beat : assignedBeats) {
            beat.setAssignedPostman(null);
        }
        beatRepository.saveAll(assignedBeats);

        postmanRepository.delete(postman);
        userRepository.delete(postman.getUser());
    }

    public Postman findPostman(UUID postmanId) {
        return postmanRepository.findById(postmanId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND.toString(), "Postman not found"));
    }

    private PostAdmin findPostAdmin(UUID postAdminId) {
        return postAdminRepository.findById(postAdminId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND.toString(), "Post admin not found"));
    }

    public PostAdminProfileResponse getAdminProfile(Long userId) {
        // Find the PostAdmin entity using the logged-in User's ID
        PostAdmin postAdmin = postAdminRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN.toString(), "Post admin profile not found"));

        // Check if the office handles deliveries (Adjust the string "Delivery" based on what is actually saved in your DB)
        boolean isDelivery = "Delivery".equalsIgnoreCase(String.valueOf(postAdmin.getOffice().getDeliveryStatus()));

        return new PostAdminProfileResponse(
                postAdmin.getId(),
                postAdmin.getEmployeeId(),
                postAdmin.getUser().getFullName(), // Pulled from the linked User entity
                postAdmin.getUser().getEmail(),    // Pulled from the linked User entity
                postAdmin.getUser().getMobileNo(), // Pulled from the linked User entity
                postAdmin.getDesignation(),
                postAdmin.getOffice().getId(),
                postAdmin.getOffice().getName(),
                postAdmin.getOffice().getPincode(),
                isDelivery
        );
    }

    private void syncBeats(Postman postman, List<UUID> beatIds) {
        List<Beat> currentBeats = beatRepository.findAll().stream()
                .filter(beat -> beat.getAssignedPostman() != null && beat.getAssignedPostman().getId().equals(postman.getId()))
                .toList();

        for (Beat beat : currentBeats) {
            beat.setAssignedPostman(null);
        }

        if (beatIds != null) {
            for (UUID beatId : beatIds) {
                Beat beat = beatRepository.findById(beatId)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND.toString(), "Beat not found"));
                if (!beat.getOffice().getId().equals(postman.getOffice().getId())) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_INPUT.toString(), "Beat and postman must belong to the same office");
                }
                beat.setAssignedPostman(postman);
            }
        }

        beatRepository.saveAll(currentBeats);
        if (beatIds != null && !beatIds.isEmpty()) {
            beatRepository.flush();
        }
    }

    private void validateOfficeAccess(Long actorUserId, UserRole actorRole, UUID officeId) {
        if (actorRole == UserRole.SUPERADMIN) {
            return;
        }
        if (actorRole != UserRole.POSTADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN.toString(), "Only post admins or super admins can manage postmen");
        }

        PostAdmin actor = postAdminRepository.findByUser_UserId(actorUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN.toString(), "Post admin profile not found"));

        if (!actor.getOffice().getId().equals(officeId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN.toString(), "You can manage postmen only for your office");
        }
    }

    private void validateUserIdentityUniqueness(Long existingUserId, String username, String email, String mobileNo) {
        if (username != null) {
            userRepository.findByUsername(username)
                    .filter(user -> !user.getUserId().equals(existingUserId))
                    .ifPresent(user -> {
                        throw new ApiException(HttpStatus.CONFLICT, ErrorCodes.USERNAME_EXISTS.toString(), "Username already exists");
                    });
        }
        if (email != null) {
            userRepository.findByEmail(email)
                    .filter(user -> !user.getUserId().equals(existingUserId))
                    .ifPresent(user -> {
                        throw new ApiException(HttpStatus.CONFLICT, ErrorCodes.EMAIL_EXISTS.toString(), "Email already exists");
                    });
        }
        if (mobileNo != null) {
            userRepository.findByMobileNo(mobileNo)
                    .filter(user -> !user.getUserId().equals(existingUserId))
                    .ifPresent(user -> {
                        throw new ApiException(HttpStatus.CONFLICT, ErrorCodes.MOBILE_EXISTS.toString(), "Mobile number already exists");
                    });
        }
    }

    private void validatePostmanEmployeeId(UUID existingPostmanId, String employeeId) {
        if (employeeId == null) {
            return;
        }
        postmanRepository.findByEmployeeId(employeeId)
                .filter(postman -> !postman.getId().equals(existingPostmanId))
                .ifPresent(postman -> {
                    throw new ApiException(HttpStatus.CONFLICT, ErrorCodes.DUPLICATE_RESOURCE.toString(), "Postman employee ID already exists");
                });
    }

    private void validatePostAdminEmployeeId(UUID existingPostAdminId, String employeeId) {
        if (employeeId == null) {
            return;
        }
        postAdminRepository.findByEmployeeId(employeeId)
                .filter(postAdmin -> !postAdmin.getId().equals(existingPostAdminId))
                .ifPresent(postAdmin -> {
                    throw new ApiException(HttpStatus.CONFLICT, ErrorCodes.DUPLICATE_RESOURCE.toString(), "Post admin employee ID already exists");
                });
    }

}




