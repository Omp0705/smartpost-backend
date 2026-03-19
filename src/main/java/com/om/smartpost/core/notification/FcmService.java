package com.om.smartpost.core.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FcmService {

    public void sendPushNotification(String token, String title, String body, String link) {
         Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putData("click_action", link) // For deep linking in app
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Sent message to token. Device response: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("FCM Error: {}", e.getMessage());
        }
    }
}
