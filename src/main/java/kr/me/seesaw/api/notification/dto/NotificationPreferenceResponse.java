package kr.me.seesaw.api.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.me.seesaw.core.domain.notification.NotificationPreference;
import kr.me.seesaw.core.domain.notification.NotificationType;

@Schema(description = "알림 수신 설정 응답")
public record NotificationPreferenceResponse(
        NotificationType type,
        boolean inAppEnabled,
        boolean webPushEnabled,
        boolean emailEnabled
) {

    public NotificationPreferenceResponse(NotificationPreference preference) {
        this(preference.getType(), preference.isInAppEnabled(), preference.isWebPushEnabled(), preference.isEmailEnabled());
    }

}
