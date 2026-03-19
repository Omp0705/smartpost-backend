package com.om.smartpost.profile.service;

import com.om.smartpost.profile.dto.request.AddressCreateRequest;
import com.om.smartpost.profile.dto.request.DeliveryPreferencesUpdateRequest;
import com.om.smartpost.profile.dto.response.DeliveryPreferencesResponse;
import com.om.smartpost.profile.dto.response.DeliverySlotResponse;
import com.om.smartpost.profile.dto.response.SavedAddressResponse;
import com.om.smartpost.profile.dto.response.SavedAddressSummaryResponse;
import com.om.smartpost.profile.dto.response.UserProfileResponse;
import com.om.smartpost.profile.entity.DeliveryPreference;
import com.om.smartpost.profile.entity.SavedAddress;
import com.om.smartpost.core.identity.entity.User;
import com.om.smartpost.core.error.ErrorCodes;
import com.om.smartpost.core.exception.ApiException;
import com.om.smartpost.shipment.enums.DeliverySlot;
import com.om.smartpost.profile.repository.DeliveryPreferenceRepository;
import com.om.smartpost.profile.repository.SavedAddressRepository;
import com.om.smartpost.core.identity.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final SavedAddressRepository addressRepository;
    private final DeliveryPreferenceRepository preferenceRepository;

    public UserProfileResponse getProfile(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCodes.USER_NOT_FOUND.toString(), "User not found"));

        // Addresses
        List<SavedAddressSummaryResponse> addressList =
                addressRepository.findByUser_UserId(user.getUserId())
                        .stream()
                        .map(addr -> new SavedAddressSummaryResponse(
                                addr.getId(),
                                addr.getType(),
                                addr.getAddressLine1(),
                                addr.getAddressLine2(),
                                addr.getCity(),
                                addr.getPincode(),
                                addr.isDefault()
                        ))
                        .toList();

        // Preferences
        DeliveryPreference pref =
                preferenceRepository.findByUser_UserId(user.getUserId())
                        .orElse(null);

        DeliveryPreferencesResponse preferenceResponse = null;

        if (pref != null) {
            preferenceResponse = new DeliveryPreferencesResponse(
                    DeliverySlotResponse.fromEnum(pref.getPreferedDeliverySlot()),
                    pref.isLeaveAtDoor(),
                    pref.isLeaveWithGuard(),
                    pref.isDeliverToNeighbor(),
                    pref.isCallBeforeDelivery(),
                    pref.isOtpRequired(),
                    pref.isSignatureRequired(),
                    pref.isAvoidMorning(),
                    pref.isWeekendOnly(),
                    pref.getDeliveryNote()
            );
        }

        return new UserProfileResponse(
                user.getFullName(),
                user.getEmail(),
                user.getMobileNo(),
                user.getProfilePictureUrl(),
                addressList,
                preferenceResponse
        );
    }


    @Transactional
    public void updateFcmToken(Long userId, String token) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found"));

        // Only update if the token is different to avoid unnecessary DB writes
        if (token != null && !token.equals(user.getFcmToken())) {
            user.setFcmToken(token);
            userRepository.save(user);
        }
    }

    @Transactional
    public DeliveryPreferencesResponse updateDeliveryPreferences(
            Long userId,
            DeliveryPreferencesUpdateRequest request
    ) {

        DeliveryPreference preference = preferenceRepository.findByUser_UserId(userId)
                .orElseGet(() -> createNewPreference(userId));

        if (request.getPreferredDeliverySlot() != null){
            preference.setPreferedDeliverySlot(
                    DeliverySlot.valueOf(request.getPreferredDeliverySlot().getCode())
            );
        }

        if (request.getLeaveAtDoor() != null)
            preference.setLeaveAtDoor(request.getLeaveAtDoor());

        if (request.getLeaveWithGuard() != null)
            preference.setLeaveWithGuard(request.getLeaveWithGuard());

        if (request.getDeliverToNeighbor() != null)
            preference.setDeliverToNeighbor(request.getDeliverToNeighbor());

        if (request.getCallBeforeDelivery() != null)
            preference.setCallBeforeDelivery(request.getCallBeforeDelivery());

        if (request.getOtpRequired() != null)
            preference.setOtpRequired(request.getOtpRequired());

        if (request.getSignatureRequired() != null)
            preference.setSignatureRequired(request.getSignatureRequired());

        if (request.getAvoidMorning() != null)
            preference.setAvoidMorning(request.getAvoidMorning());

        if (request.getWeekendOnly() != null)
            preference.setWeekendOnly(request.getWeekendOnly());

        if (request.getDeliveryNote() != null)
            preference.setDeliveryNote(request.getDeliveryNote());

        return toDeliveryPreferencesResponse(preferenceRepository.save(preference));
    }

//    helper method to create empty preference
    private DeliveryPreference createNewPreference(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCodes.USER_NOT_FOUND.toString(), "User not found"));

        DeliveryPreference preference = new DeliveryPreference();
        preference.setUser(user);
        return preferenceRepository.save(preference);
    }

//    Add new Address
    @Transactional
    public SavedAddressResponse addAddress(
            Long userId,
            AddressCreateRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCodes.USER_NOT_FOUND.toString(), "User not found"));
        SavedAddress address = new SavedAddress();
        address.setUser(user);
        address.setType(request.getType());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setPincode(request.getPincode());
        boolean makeDefault = Boolean.TRUE.equals(request.getIsDefault());

        if (makeDefault || addressRepository.findByUser_UserId(userId).isEmpty()) {
            clearDefaultForUser(userId);
            address.setDefault(true);
        }
        return mapToAddressResponse(addressRepository.save(address));
    }

//    Update the existing address
    @Transactional
    public SavedAddressResponse updateAddress(
            UUID addressId,
            AddressCreateRequest request
    ) {

    SavedAddress address = addressRepository.findById(addressId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND.toString(), "Address not found"));

    if (request.getType() != null)
        address.setType(request.getType());

    if (request.getAddressLine1() != null)
        address.setAddressLine1(request.getAddressLine1());

    if (request.getAddressLine2() != null)
        address.setAddressLine2(request.getAddressLine2());

    if (request.getCity() != null)
        address.setCity(request.getCity());

    if (request.getPincode() != null)
        address.setPincode(request.getPincode());

    if (request.getIsDefault() != null && request.getIsDefault()) {
        clearDefaultForUser(address.getUser().getUserId());
        address.setDefault(true);
    }

    return mapToAddressResponse(addressRepository.save(address));
}

//    Delete Existing address
    @Transactional
    public void deleteAddress(UUID addressId)
    {

    SavedAddress address = addressRepository.findById(addressId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND.toString(), "Address not found"));

    Long userId = address.getUser().getUserId();
    boolean wasDefault = address.isDefault();

    addressRepository.delete(address);

    if (wasDefault) {
        List<SavedAddress> remaining =
                addressRepository.findByUser_UserId(userId);

        if (!remaining.isEmpty()) {
            remaining.get(0).setDefault(true);
            addressRepository.save(remaining.get(0));
        }
    }
}



    //  IF only one address exists make that address the default one
    private void clearDefaultForUser(Long userId) {
        List<SavedAddress> addresses =
                addressRepository.findByUser_UserId(userId);

        for (SavedAddress addr : addresses) {
            addr.setDefault(false);
        }

        addressRepository.saveAll(addresses);
    }
    //    Mapper Method nothing much than that
    private DeliveryPreferencesResponse toDeliveryPreferencesResponse(DeliveryPreference p) {
        return new DeliveryPreferencesResponse(

                DeliverySlotResponse.fromEnum(p.getPreferedDeliverySlot()),
                p.isLeaveAtDoor(),
                p.isLeaveWithGuard(),
                p.isDeliverToNeighbor(),
                p.isCallBeforeDelivery(),
                p.isOtpRequired(),
                p.isSignatureRequired(),
                p.isAvoidMorning(),
                p.isWeekendOnly(),
                p.getDeliveryNote()
        );
    }

    private SavedAddressResponse mapToAddressResponse(SavedAddress savedAddress) {
        return new SavedAddressResponse(
                savedAddress.getType(),
                savedAddress.getAddressLine1(),
                savedAddress.getAddressLine2(),
                savedAddress.getCity(),
                savedAddress.getPincode(),
                savedAddress.isDefault()
        );
    }
}







