package com.om.smartpost.notification.controller;

import com.om.smartpost.core.security.AuthenticatedUserFacade;
import com.om.smartpost.notification.dto.response.NotificationResponse;
import com.om.smartpost.notification.entities.Notification;
import com.om.smartpost.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthenticatedUserFacade authenticatedUserFacade;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(Authentication authentication) {
        Long currentUserId = authenticatedUserFacade.currentUserId(authentication);
        return ResponseEntity.ok(notificationService.getUserNotifications(currentUserId));
    }

//    @PutMapping("/{id}")
//    public ResponseEntity<NotificationResponse> updateNotification(
//            @PathVariable("id") Long id,
//            @Valid @RequestBody NotificationUpdateRequest request,
//            Authentication authentication) {
//
//        Long currentUserId = authenticatedUserFacade.currentUserId(authentication);
//        return ResponseEntity.ok(notificationService.updateNotificationStatus(id, currentUserId, request));
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable("id") Long id, Authentication authentication) {
        Long currentUserId = authenticatedUserFacade.currentUserId(authentication);
        notificationService.deleteNotification(id, currentUserId);

        return ResponseEntity.noContent().build();
    }
}