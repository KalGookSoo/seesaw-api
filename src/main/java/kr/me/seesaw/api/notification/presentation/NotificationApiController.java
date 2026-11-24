package kr.me.seesaw.api.notification.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import kr.me.seesaw.api.notification.NotificationInboxService;
import kr.me.seesaw.api.notification.dto.NotificationResponse;
import kr.me.seesaw.core.support.pattern.PatternMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/api/notifications")
public class NotificationApiController {

    private final NotificationInboxService notificationInboxService;

    @PreAuthorize("isAuthenticated() and @siteAuthorizationService.hasRoleForSite(#siteId)")
    @GetMapping
    public ResponseEntity<PagedModel<NotificationResponse>> findAll(
            @RequestParam @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String siteId,
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<NotificationResponse> page = notificationInboxService.findAll(siteId, pageable);
        return ResponseEntity.ok(new PagedModel<>(page));
    }

    @PreAuthorize("isAuthenticated() and @siteAuthorizationService.hasRoleForSite(#siteId)")
    @GetMapping("/unread-count")
    public ResponseEntity<Long> countUnread(
            @RequestParam @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String siteId
    ) {
        return ResponseEntity.ok(notificationInboxService.countUnread(siteId));
    }

    @PreAuthorize("isAuthenticated() and @siteAuthorizationService.hasRoleForSite(#siteId)")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @RequestParam @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String siteId,
            @PathVariable @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String notificationId
    ) {
        return ResponseEntity.ok(notificationInboxService.markAsRead(siteId, notificationId));
    }

    @PreAuthorize("isAuthenticated() and @siteAuthorizationService.hasRoleForSite(#siteId)")
    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @RequestParam @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String siteId
    ) {
        notificationInboxService.markAllAsRead(siteId);
        return ResponseEntity.noContent().build();
    }

}
