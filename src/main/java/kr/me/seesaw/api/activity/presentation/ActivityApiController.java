package kr.me.seesaw.api.activity.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import kr.me.seesaw.api.activity.application.ActivityService;
import kr.me.seesaw.api.activity.dto.ActivityResponse;
import kr.me.seesaw.api.security.SiteAuthorizationService;
import kr.me.seesaw.core.support.pattern.PatternMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/activity")
@RequiredArgsConstructor
@Validated
public class ActivityApiController {
    private final ActivityService activityService;
    private final SiteAuthorizationService siteAuthorizationService;

    @GetMapping("/{type}")
    @PreAuthorize("isAuthenticated()")
    public PagedModel<ActivityResponse> find(
            @PathVariable @Pattern(regexp = "articles|replies|likes") String type,
            @RequestParam @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String siteId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        if (!siteAuthorizationService.hasRoleForSite(siteId)) {
            throw new AccessDeniedException("사이트에 접근할 권한이 없습니다.");
        }
        return new PagedModel<>(activityService.find(siteId, type, pageable));
    }
}
