package kr.me.seesaw.api.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.me.seesaw.core.domain.notification.Notification;
import kr.me.seesaw.core.domain.notification.NotificationTargetType;
import kr.me.seesaw.core.domain.notification.NotificationType;
import kr.me.seesaw.core.support.dto.BaseResponse;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(name = "NotificationResponse", description = "인앱 알림 응답")
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class NotificationResponse extends BaseResponse {

    private final NotificationType type;
    private final String title;
    private final String message;
    private final NotificationTargetType targetType;
    private final String targetId;
    private final String targetUrl;
    private final String actorUsername;
    private final boolean read;
    private final LocalDateTime readAt;
    private final LocalDateTime expiresAt;

    public NotificationResponse(Notification notification) {
        setBaseModel(notification);
        this.type = notification.getType();
        this.title = notification.getTitle();
        this.message = notification.getMessage();
        this.targetType = notification.getTargetType();
        this.targetId = notification.getTargetId();
        this.targetUrl = notification.getTargetUrl();
        this.actorUsername = notification.getActorUsername();
        this.read = notification.isRead();
        this.readAt = notification.getReadAt();
        this.expiresAt = notification.getExpiresAt();
    }

}
