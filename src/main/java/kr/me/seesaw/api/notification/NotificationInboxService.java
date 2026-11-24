package kr.me.seesaw.api.notification;

import kr.me.seesaw.api.notification.dto.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationInboxService {

    Page<NotificationResponse> findAll(String siteId, Pageable pageable);

    long countUnread(String siteId);

    NotificationResponse markAsRead(String siteId, String notificationId);

    void markAllAsRead(String siteId);

}
