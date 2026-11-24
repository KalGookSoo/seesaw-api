package kr.me.seesaw.api.notification;

import kr.me.seesaw.api.notification.dto.NotificationPreferenceRequest;
import kr.me.seesaw.api.notification.dto.NotificationPreferenceResponse;

public interface NotificationPreferenceService {

    NotificationPreferenceResponse get(String siteId, String username, kr.me.seesaw.core.domain.notification.NotificationType type);

    NotificationPreferenceResponse update(String siteId, String username, NotificationPreferenceRequest request);

}
