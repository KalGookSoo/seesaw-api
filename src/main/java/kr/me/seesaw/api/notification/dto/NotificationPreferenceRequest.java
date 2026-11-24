package kr.me.seesaw.api.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.me.seesaw.core.domain.notification.NotificationType;

@Schema(description = "알림 수신 설정 변경 요청")
public record NotificationPreferenceRequest(
        @NotNull NotificationType type,
        boolean webPushEnabled,
        boolean emailEnabled
) {
}
