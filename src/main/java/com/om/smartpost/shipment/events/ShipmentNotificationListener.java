package com.om.smartpost.shipment.events;

import com.om.smartpost.shipment.entity.Shipment;
import com.om.smartpost.core.identity.entity.User;
import com.om.smartpost.shipment.notification.repository.NotificationRepository;
import com.om.smartpost.core.notification.FcmService;
import com.om.smartpost.core.notification.MagicLinkService;
import com.om.smartpost.core.notification.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentNotificationListener {

    private final SmsService smsService;
    private final MagicLinkService magicLinkService;
    private final FcmService fcmService;
    private final NotificationRepository notificationRepository;

    @Async
    @EventListener
    public void handleShipmentCreated(ShipmentCreatedEvent event) {
        Shipment shipment = event.shipment();
        User receiver = shipment.getReceiverUser();
        String mobile = shipment.getReceiverDetails().getMobileNo();

        String token = magicLinkService.generateTrackingToken(shipment.getTrackingNumber());

        if (receiver != null) {
            //  REGISTERED USER
            com.om.smartpost.shipment.notification.entity.Notification dbNotif = new com.om.smartpost.shipment.notification.entity.Notification();
            dbNotif.setRecipient(receiver);
            dbNotif.setTitle("New Shipment Received!");
            dbNotif.setMessage("A parcel with tracking ID " + shipment.getTrackingNumber() + " is headed to you.");
            dbNotif.setLink("/tracking/" + shipment.getTrackingNumber());
            notificationRepository.save(dbNotif);

            //  Send Push Notification if FCM token exists
            if (receiver.getFcmToken() != null && !receiver.getFcmToken().isBlank()) {
                fcmService.sendPushNotification(
                        receiver.getFcmToken(),
                        "Parcel Tracking",
                        "You have a new shipment: " + shipment.getTrackingNumber(),
                        "/tracking/" + shipment.getTrackingNumber()
                );
            }
        } else {
            //  UNREGISTERED USER: Send SMS with Magic Link
            log.info("Receiver not registered. Falling back to SMS for: {}", mobile);
            smsService.sendTrackingMagicLink(mobile, shipment.getTrackingNumber(), token);
        }
    }
}
