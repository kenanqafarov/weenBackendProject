package com.ween.service;

// Firebase Service (DISABLED - Firebase dependency commented out)
/*
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FirebaseService {

    private final FirebaseMessaging firebaseMessaging;

    public void sendNotification(String deviceToken, String title, String body) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setNotification(notification)
                    .setToken(deviceToken)
                    .build();

            String response = firebaseMessaging.send(message);
            log.info("Firebase notification sent with ID: {} to device: {}", response, deviceToken);
        } catch (Exception e) {
            log.error("Failed to send Firebase notification to device: {}", deviceToken, e);
            throw new RuntimeException("Failed to send Firebase notification", e);
        }
    }

    public void sendNotificationWithData(String deviceToken, String title, String body, Map<String, String> data) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setNotification(notification)
                    .putAllData(data)
                    .setToken(deviceToken)
                    .build();

            String response = firebaseMessaging.send(message);
            log.info("Firebase notification with data sent with ID: {} to device: {}", response, deviceToken);
        } catch (Exception e) {
            log.error("Failed to send Firebase notification with data to device: {}", deviceToken, e);
            throw new RuntimeException("Failed to send Firebase notification", e);
        }
    }

    public void sendMulticast(List<String> deviceTokens, String title, String body) {
        if (deviceTokens == null || deviceTokens.isEmpty()) {
            log.warn("No device tokens provided for multicast notification");
            return;
        }

        for (String deviceToken : deviceTokens) {
            try {
                sendNotification(deviceToken, title, body);
            } catch (Exception e) {
                log.warn("Failed to send notification to device token: {}", deviceToken, e);
            }
        }
    }

    public void sendMulticastWithData(List<String> deviceTokens, String title, String body, Map<String, String> data) {
        if (deviceTokens == null || deviceTokens.isEmpty()) {
            log.warn("No device tokens provided for multicast notification");
            return;
        }

        for (String deviceToken : deviceTokens) {
            try {
                sendNotificationWithData(deviceToken, title, body, data);
            } catch (Exception e) {
                log.warn("Failed to send notification with data to device token: {}", deviceToken, e);
            }
        }
    }

    public void sendEventNotification(String deviceToken, String eventTitle, String eventCity) {
        Map<String, String> data = new HashMap<>();
        data.put("type", "EVENT_NOTIFICATION");
        data.put("eventTitle", eventTitle);
        data.put("eventCity", eventCity);

        sendNotificationWithData(
                deviceToken,
                "New Event Available",
                eventTitle + " in " + eventCity,
                data
        );
    }

    public void sendRegistrationConfirmation(String deviceToken, String eventTitle) {
        Map<String, String> data = new HashMap<>();
        data.put("type", "REGISTRATION_CONFIRMATION");
        data.put("eventTitle", eventTitle);

        sendNotificationWithData(
                deviceToken,
                "Registration Confirmed",
                "You have successfully registered for " + eventTitle,
                data
        );
    }

    public void sendCertificateReadyNotification(String deviceToken, String eventTitle, String certificateNumber) {
        Map<String, String> data = new HashMap<>();
        data.put("type", "CERTIFICATE_READY");
        data.put("eventTitle", eventTitle);
        data.put("certificateNumber", certificateNumber);

        sendNotificationWithData(
                deviceToken,
                "Certificate Ready",
                "Your certificate for " + eventTitle + " is ready to download",
                data
        );
    }

    public void sendCoinRewardNotification(String deviceToken, Integer coins, String reason) {
        Map<String, String> data = new HashMap<>();
        data.put("type", "COIN_REWARD");
        data.put("coins", coins.toString());
        data.put("reason", reason);

        sendNotificationWithData(
                deviceToken,
                "Ween Coins Earned!",
                "You earned " + coins + " coins for " + reason,
                data
        );
    }

    public void sendCheckinReminderNotification(String deviceToken, String eventTitle, String eventDate) {
        Map<String, String> data = new HashMap<>();
        data.put("type", "CHECKIN_REMINDER");
        data.put("eventTitle", eventTitle);
        data.put("eventDate", eventDate);

        sendNotificationWithData(
                deviceToken,
                "Event Starting Soon",
                "Don't forget to check in for " + eventTitle,
                data
        );
    }
}
*/
