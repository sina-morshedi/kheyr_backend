package com.nano_electronics_cital.kheyr_backend.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.nano_electronics_cital.kheyr_backend.model.Conversation;
import com.nano_electronics_cital.kheyr_backend.model.DeviceToken;
import com.nano_electronics_cital.kheyr_backend.model.embedded.ConversationParticipant;
import com.nano_electronics_cital.kheyr_backend.repository.DeviceTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

@Service
public class FcmNotificationService {

    private static final Logger log = LoggerFactory.getLogger(FcmNotificationService.class);
    private static final String DEFAULT_ANDROID_CHANNEL_ID = "kheyr_chat_messages";

    private final DeviceTokenRepository deviceTokenRepository;

    @Value("${firebase.enabled:false}")
    private boolean firebaseEnabled;

    @Value("${firebase.android-alert-channel-id:" + DEFAULT_ANDROID_CHANNEL_ID + "}")
    private String androidAlertChannelId;

    public FcmNotificationService(DeviceTokenRepository deviceTokenRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
    }

    public void notifyMessageCreated(
            Conversation conversation,
            com.nano_electronics_cital.kheyr_backend.model.Message message
    ) {
        if (!firebaseEnabled || FirebaseApp.getApps().isEmpty()) {
            log.info(
                    "KHEYR_FCM_BACKEND skip notifyMessageCreated | firebaseEnabled={} | firebaseApps={} | conversationId={} | messageId={}",
                    firebaseEnabled,
                    FirebaseApp.getApps().size(),
                    conversation.getId(),
                    message.getId()
            );
            return;
        }

        String senderDisplayName = resolveSenderDisplayName(conversation, message.getSenderUserId());
        String title = resolveNotificationTitle(conversation, senderDisplayName);
        String body = resolveNotificationBody(message, senderDisplayName, conversation.getTitle());

        Map<String, String> data = new HashMap<>();
        data.put("type", "chat_message");
        data.put("conversationId", conversation.getId());
        data.put("messageId", message.getId());
        data.put("senderUserId", message.getSenderUserId());
        data.put("senderDisplayName", senderDisplayName);
        data.put("title", title);
        data.put("body", body);
        if (StringUtils.hasText(conversation.getTitle())) {
            data.put("conversationTitle", conversation.getTitle().trim());
        }

        log.info(
                "KHEYR_FCM_BACKEND notifyMessageCreated prepared | conversationId={} | messageId={} | senderUserId={} | participantCount={} | title={} | body={} | data={}",
                conversation.getId(),
                message.getId(),
                message.getSenderUserId(),
                conversation.getParticipantIds().size(),
                title,
                body,
                data
        );

        for (String participantId : conversation.getParticipantIds()) {
            if (participantId.equals(message.getSenderUserId())) {
                continue;
            }

            sendToUserDevices(participantId, title, body, data);
        }
    }

    public void sendToUserDevices(
            String userId,
            String title,
            String body,
            Map<String, String> data
    ) {
        List<DeviceToken> devices = deviceTokenRepository.findAllByUserIdAndActiveTrue(userId);
        log.info(
                "KHEYR_FCM_BACKEND resolved target devices | userId={} | activeDeviceCount={} | title={} | body={} | dataKeys={}",
                userId,
                devices.size(),
                title,
                body,
                data.keySet()
        );

        if (devices.isEmpty()) {
            log.warn(
                    "KHEYR_FCM_BACKEND no active devices found for user | userId={} | title={} | body={}",
                    userId,
                    title,
                    body
            );
        }

        for (DeviceToken device : devices) {
            try {
                log.info(
                        "KHEYR_FCM_BACKEND sending to device | userId={} | tokenId={} | platform={} | tokenSuffix={}",
                        userId,
                        device.getId(),
                        device.getPlatform(),
                        maskToken(device.getDeviceToken())
                );
                String firebaseMessageId = sendToDevice(device, title, body, data);
                log.info(
                        "KHEYR_FCM_BACKEND send success | userId={} | tokenId={} | platform={} | firebaseMessageId={}",
                        userId,
                        device.getId(),
                        device.getPlatform(),
                        firebaseMessageId
                );
            } catch (FirebaseMessagingException exception) {
                log.warn(
                        "KHEYR_FCM_BACKEND send failed | userId={} | tokenId={} | platform={} | tokenSuffix={} | errorCode={}",
                        userId,
                        device.getId(),
                        device.getPlatform(),
                        maskToken(device.getDeviceToken()),
                        exception.getErrorCode()
                );

                if (shouldDeactivateToken(exception)) {
                    device.setActive(false);
                    deviceTokenRepository.save(device);
                }
            } catch (Exception exception) {
                log.warn(
                        "KHEYR_FCM_BACKEND unexpected send error | userId={} | tokenId={} | platform={} | tokenSuffix={}",
                        userId,
                        device.getId(),
                        device.getPlatform(),
                        maskToken(device.getDeviceToken()),
                        exception
                );
            }
        }
    }

    public String sendToDevice(
            DeviceToken device,
            String title,
            String body,
            Map<String, String> data
    ) throws FirebaseMessagingException {
        String platform = device.getPlatform() != null
                ? device.getPlatform().trim().toLowerCase(Locale.ROOT)
                : "";

        if ("android".equals(platform)) {
            return sendAndroidDataMessage(device.getDeviceToken(), data);
        }

        return sendToToken(device.getDeviceToken(), title, body, data);
    }

    public String sendToToken(
            String token,
            String title,
            String body,
            Map<String, String> data
    ) throws FirebaseMessagingException {
        Message firebaseMessage = Message.builder()
                .setToken(token)
                .putAllData(data)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setNotification(AndroidNotification.builder()
                                .setChannelId(androidAlertChannelId)
                                .setSound("default")
                                .build())
                        .build())
                .setApnsConfig(ApnsConfig.builder()
                        .putHeader("apns-priority", "10")
                        .setAps(Aps.builder().setSound("default").build())
                        .build())
                .build();

        return FirebaseMessaging.getInstance().send(firebaseMessage);
    }

    public String sendAndroidDataMessage(
            String token,
            Map<String, String> data
    ) throws FirebaseMessagingException {
        Message firebaseMessage = Message.builder()
                .setToken(token)
                .putAllData(data)
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .build())
                .build();

        return FirebaseMessaging.getInstance().send(firebaseMessage);
    }

    private boolean shouldDeactivateToken(FirebaseMessagingException exception) {
        String errorCode = exception.getErrorCode() != null
                ? exception.getErrorCode().name()
                : "";

        return "REGISTRATION_TOKEN_NOT_REGISTERED".equalsIgnoreCase(errorCode)
                || "INVALID_ARGUMENT".equalsIgnoreCase(errorCode);
    }

    private String maskToken(String token) {
        if (!StringUtils.hasText(token)) {
            return "<empty>";
        }

        String trimmed = token.trim();
        if (trimmed.length() <= 8) {
            return trimmed;
        }

        return "..." + trimmed.substring(trimmed.length() - 8);
    }

    private String resolveNotificationTitle(Conversation conversation, String senderDisplayName) {
        if (StringUtils.hasText(conversation.getTitle())) {
            return conversation.getTitle().trim();
        }

        return senderDisplayName;
    }

    private String resolveNotificationBody(
            com.nano_electronics_cital.kheyr_backend.model.Message message,
            String senderDisplayName,
            String conversationTitle
    ) {
        String preview = buildMessagePreview(message);
        if (StringUtils.hasText(conversationTitle)) {
            return senderDisplayName + ": " + preview;
        }

        return preview;
    }

    private String resolveSenderDisplayName(Conversation conversation, String senderUserId) {
        for (ConversationParticipant participant : conversation.getParticipants()) {
            if (!participant.getUserId().equals(senderUserId)) {
                continue;
            }

            if (StringUtils.hasText(participant.getDisplayName())) {
                return participant.getDisplayName().trim();
            }

            String fullName = (participant.getFirstName() + " " + participant.getLastName()).trim();
            if (StringUtils.hasText(fullName)) {
                return fullName;
            }
        }

        return "New message";
    }

    private String buildMessagePreview(
            com.nano_electronics_cital.kheyr_backend.model.Message message
    ) {
        if (message.isDeletedForEveryone()) {
            return "This message was deleted.";
        }

        if (StringUtils.hasText(message.getContent())) {
            String content = message.getContent().trim();
            return content.length() > 120 ? content.substring(0, 120) : content;
        }

        return switch (message.getType()) {
            case IMAGE -> "[Image]";
            case VIDEO -> "[Video]";
            case AUDIO -> "[Audio]";
            case FILE -> "[File]";
            case LOCATION -> "[Location]";
            case SYSTEM -> "[System]";
            case TEXT -> "[Message]";
        };
    }
}
