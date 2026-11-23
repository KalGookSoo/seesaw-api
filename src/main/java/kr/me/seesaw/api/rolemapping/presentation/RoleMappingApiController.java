package kr.me.seesaw.api.rolemapping.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import kr.me.seesaw.api.rolemapping.RoleMappingService;
import kr.me.seesaw.core.support.pattern.PatternMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/role-mappings")
public class RoleMappingApiController {

    private final RoleMappingService roleMappingService;

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/me")
    public ResponseEntity<Void> removeMyRoleMapping(@RequestParam @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String siteId) {
        roleMappingService.removeMyRoleMapping(siteId);
        return ResponseEntity.noContent().build();
    }

}
