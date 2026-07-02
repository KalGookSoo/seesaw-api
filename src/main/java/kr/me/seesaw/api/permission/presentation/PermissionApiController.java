package kr.me.seesaw.api.permission.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import kr.me.seesaw.api.permission.dto.SavePermissionRequest;
import kr.me.seesaw.api.permission.dto.PermissionResponse;
import kr.me.seesaw.api.service.PermissionService;
import kr.me.seesaw.core.support.pattern.PatternMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/api/permissions")
public class PermissionApiController {

    private final PermissionService permissionService;

    @GetMapping
    public ResponseEntity<Map<String, List<PermissionResponse>>> getPermissionsByTargetId(@RequestParam("targetId") @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String targetId) {
        List<PermissionResponse> permissions = permissionService.getPermissionsByTargetId(targetId);
        return ResponseEntity.ok(Map.of("permissions", permissions));
    }

    @PostMapping
    public ResponseEntity<Map<String, PermissionResponse>> savePermission(@Valid @RequestBody SavePermissionRequest command) {
        PermissionResponse permission = permissionService.savePermission(command);
        return ResponseEntity.ok(Map.of("permission", permission));
    }

}
