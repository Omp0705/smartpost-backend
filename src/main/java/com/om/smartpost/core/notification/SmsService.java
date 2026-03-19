package com.om.smartpost.core.notification;

import com.twilio.http.TwilioRestClient;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    // Using @Value here because these are DIFFERENT from your main TwilioConfig
    @Value("${twilio.account_sid}")
    private String smsAccountSid;

    @Value("${twilio.auth_token}")
    private String smsAuthToken;

    @Value("${twilio.virtual_number}")
    private String fromPhoneNumber;

    @Value("${app.frontend_url}")
    private String frontendUrl;

    public void sendTrackingMagicLink(String receiverMobile, String trackingNumber, String token) {
        String trackingLink = String.format("%s/tracking.html?id=%s&token=%s",
                frontendUrl, trackingNumber, token);

        String body = String.format(
                "SmartPost: Your shipment %s is being processed. Track or manage your delivery here: %s Register on the app to stay updated.",
                trackingNumber, trackingLink
        );

        sendSms(receiverMobile, body);
    }

    private void sendSms(String receiverMobile, String body) {
        try {
            // 1. Create a dedicated client for THIS specific SMS account
            // This ignores whatever was set in TwilioInitializer
            TwilioRestClient client = new TwilioRestClient.Builder(smsAccountSid, smsAuthToken).build();

            String to = receiverMobile.startsWith("+") ? receiverMobile : "+91" + receiverMobile;

            // 2. Pass the custom client to the creator
            Message message = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(fromPhoneNumber),
                    body
            ).create(client); // <-- Key change: passing the explicit client

            log.info("Magic Link SMS sent to {}. SID: {}", to, message.getSid());
        } catch (Exception e) {
            log.error("Twilio SMS Error for {}: {}", receiverMobile, e.getMessage());
        }
    }
}
