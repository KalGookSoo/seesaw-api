package kr.me.seesaw.api.notification.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import kr.me.seesaw.api.notification.NotificationPreferenceService;
import kr.me.seesaw.api.notification.dto.NotificationPreferenceRequest;
import kr.me.seesaw.api.notification.dto.NotificationPreferenceResponse;
import kr.me.seesaw.core.domain.notification.NotificationType;
import kr.me.seesaw.core.support.authentication.PrincipalProvider;
import kr.me.seesaw.core.support.pattern.PatternMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/api/notification-preferences")
public class NotificationPreferenceApiController {

    private final NotificationPreferenceService preferenceService;

    private final PrincipalProvider principalProvider;

    @PreAuthorize("isAuthenticated() and @siteAuthorizationService.hasRoleForSite(#siteId)")
    @GetMapping
    public ResponseEntity<NotificationPreferenceResponse> get(
            @RequestParam @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String siteId,
            @RequestParam NotificationType type
    ) {
        return ResponseEntity.ok(preferenceService.get(siteId, getUsername(), type));
    }

    @PreAuthorize("isAuthenticated() and @siteAuthorizationService.hasRoleForSite(#siteId)")
    @PutMapping
    public ResponseEntity<NotificationPreferenceResponse> update(
            @RequestParam @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String siteId,
            @Valid @RequestBody NotificationPreferenceRequest request
    ) {
        return ResponseEntity.ok(preferenceService.update(siteId, getUsername(), request));
    }

    private String getUsername() {
        Authentication authentication = principalProvider.getAuthentication();
        return authentication.getName();
    }

}
