package kr.me.seesaw.api.notification.application;

import kr.me.seesaw.api.notification.HelpdeskService;
import kr.me.seesaw.api.notification.MailService;
import kr.me.seesaw.api.user.dto.UserResponse;
import kr.me.seesaw.core.domain.site.Site;
import kr.me.seesaw.core.domain.site.SiteRepository;
import kr.me.seesaw.core.support.authentication.PrincipalProvider;
import kr.me.seesaw.core.support.message.CmsMessageSource;
import kr.me.seesaw.framework.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Transactional
@RequiredArgsConstructor
@Service
public class DefaultHelpdeskService implements HelpdeskService {

    private final SiteRepository siteRepository;
    private final PrincipalProvider principalProvider;
    private final MailProperties properties;
    private final CmsMessageSource messageSource;
    private final MailService mailService;

    @Override
    public void sendToHelpdesk(String siteId, String title, String content) {
        UserResponse user = getAuthenticatedUser();
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("사이트를 찾을 수 없습니다. siteId: " + siteId));
        String from = user.getEmail();
        String subject = String.format("[%s][%s] %s", messageSource.getMessage("label.inquiry"), site.getName(), title);
        Map<String, String> values = Map.of(
                "from", from,
                "siteName", site.getName(),
                "title", title,
                "content", content
        );
        mailService.send(from, new String[]{properties.getUsername()}, subject, "mail/send_to_helpdesk", values);
    }

    private UserResponse getAuthenticatedUser() {
        Authentication authentication = principalProvider.getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AccessDeniedException("계정 정보를 찾을 수 없습니다.");
        }
        return principal.getUser();
    }

}
