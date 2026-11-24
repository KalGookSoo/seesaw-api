package kr.me.seesaw.api.notification.event;

import kr.me.seesaw.core.domain.notification.NotificationType;

public record NotificationCreatedEvent(
        String notificationId,
        String siteId,
        String recipientUsername,
        NotificationType type,
        String title,
        String message,
        String targetUrl
) {
}
