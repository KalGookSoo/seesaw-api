package kr.me.seesaw.api.notification.application;

import kr.me.seesaw.api.notification.NotificationPreferenceService;
import kr.me.seesaw.api.notification.dto.NotificationPreferenceRequest;
import kr.me.seesaw.api.notification.dto.NotificationPreferenceResponse;
import kr.me.seesaw.core.domain.notification.NotificationPreference;
import kr.me.seesaw.core.domain.notification.NotificationPreferenceRepository;
import kr.me.seesaw.core.domain.notification.NotificationType;
import kr.me.seesaw.core.domain.site.Site;
import kr.me.seesaw.core.domain.site.SiteRepository;
import kr.me.seesaw.core.domain.user.User;
import kr.me.seesaw.core.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class DefaultNotificationPreferenceService implements NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;

    private final SiteRepository siteRepository;

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public NotificationPreferenceResponse get(String siteId, String username, NotificationType type) {
        return preferenceRepository.findBySiteIdAndUserUsernameAndType(siteId, username, type)
                .map(NotificationPreferenceResponse::new)
                .orElse(new NotificationPreferenceResponse(type, true, false, false));
    }

    @Override
    public NotificationPreferenceResponse update(String siteId, String username, NotificationPreferenceRequest request) {
        Site site = siteRepository.findById(siteId).orElseThrow();
        User user = userRepository.findByUsername(username).orElseThrow();
        NotificationPreference preference = preferenceRepository.findBySiteIdAndUserUsernameAndType(siteId, username, request.type())
                .orElseGet(NotificationPreference::new);
        preference.setSite(site);
        preference.setUser(user);
        preference.setType(request.type());
        preference.setInAppEnabled(true);
        preference.setWebPushEnabled(request.webPushEnabled());
        preference.setEmailEnabled(request.emailEnabled());
        return new NotificationPreferenceResponse(preferenceRepository.save(preference));
    }

}
