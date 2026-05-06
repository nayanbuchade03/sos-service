package com.CopMap.sos.Services;

import com.CopMap.sos.Event.GiveNotification;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone.from}")
    private String fromNumber;

    @Value("${twilio.phone.to}")
    private String toNumber;

    @Async
    @EventListener
    public void handleDispatchNotification(GiveNotification event) {
        try {
            log.info("Preparing to send SMS dispatch to Officer {}...", event.getAssignedOfficerId());

            Twilio.init(accountSid, authToken);

            String messageBody = String.format(
                    "URGENT: %s reported %.2f km from your location. Please proceed immediately.",
                    event.getEmergencyType(),
                    event.getDistanceKm()
            );

            Message message = Message.creator(
                    new PhoneNumber(toNumber),
                    new PhoneNumber(fromNumber),
                    messageBody
            ).create();

            log.info("✅ Real SMS sent successfully! Twilio SID: {}", message.getSid());

        } catch (Exception e) {
            log.error("Failed to send Twilio SMS", e);
        }
    }
}