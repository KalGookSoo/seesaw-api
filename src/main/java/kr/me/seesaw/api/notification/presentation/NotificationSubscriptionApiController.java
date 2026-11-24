package kr.me.seesaw.api.notification.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import kr.me.seesaw.api.notification.NotificationSubscriptionService;
import kr.me.seesaw.core.support.authentication.PrincipalProvider;
import kr.me.seesaw.core.support.pattern.PatternMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/api/notification-subscriptions")
public class NotificationSubscriptionApiController {

    private final NotificationSubscriptionService subscriptionService;

    private final PrincipalProvider principalProvider;

    @PreAuthorize("isAuthenticated() and @siteAuthorizationService.hasManagerRoleForSite(#siteId)")
    @PutMapping
    public ResponseEntity<Void> subscribe(
            @RequestParam @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String siteId,
            @RequestParam @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String categoryId
    ) {
        subscriptionService.subscribe(siteId, getUsername(), categoryId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated() and @siteAuthorizationService.hasManagerRoleForSite(#siteId)")
    @DeleteMapping
    public ResponseEntity<Void> unsubscribe(
            @RequestParam @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String siteId,
            @RequestParam @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String categoryId
    ) {
        subscriptionService.unsubscribe(siteId, getUsername(), categoryId);
        return ResponseEntity.noContent().build();
    }

    private String getUsername() {
        Authentication authentication = principalProvider.getAuthentication();
        return authentication.getName();
    }

}
