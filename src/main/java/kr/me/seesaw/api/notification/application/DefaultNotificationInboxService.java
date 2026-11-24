package kr.me.seesaw.api.notification.application;

import kr.me.seesaw.api.notification.NotificationInboxService;
import kr.me.seesaw.api.notification.dto.NotificationResponse;
import kr.me.seesaw.core.domain.notification.Notification;
import kr.me.seesaw.core.domain.notification.NotificationRepository;
import kr.me.seesaw.core.support.authentication.PrincipalProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Transactional
@RequiredArgsConstructor
@Service
public class DefaultNotificationInboxService implements NotificationInboxService {

    private final PrincipalProvider principalProvider;

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    @Override
    public Page<NotificationResponse> findAll(String siteId, Pageable pageable) {
        String username = getUsername();
        return notificationRepository.findInbox(siteId, username, LocalDateTime.now(), pageable)
                .map(NotificationResponse::new);
    }

    @Transactional(readOnly = true)
    @Override
    public long countUnread(String siteId) {
        return notificationRepository.countUnread(siteId, getUsername(), LocalDateTime.now());
    }

    @Override
    public NotificationResponse markAsRead(String siteId, String notificationId) {
        Notification notification = getOwnedNotification(siteId, notificationId);
        notification.markAsRead();
        return new NotificationResponse(notificationRepository.save(notification));
    }

    @Override
    public void markAllAsRead(String siteId) {
        Page<NotificationResponse> page = findAll(siteId, Pageable.unpaged());
        page.forEach(response -> getOwnedNotification(siteId, response.getId()).markAsRead());
    }

    private Notification getOwnedNotification(String siteId, String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NoSuchElementException("알림을 찾을 수 없습니다: " + notificationId));
        if (!siteId.equals(notification.getSiteId()) || !getUsername().equals(notification.getRecipient().getUsername())) {
            throw new AccessDeniedException("알림에 접근할 수 없습니다.");
        }
        return notification;
    }

    private String getUsername() {
        Authentication authentication = principalProvider.getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }
        return authentication.getName();
    }

}
