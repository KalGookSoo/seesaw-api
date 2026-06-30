package kr.me.seesaw.api.framework.controller;

import jakarta.validation.Valid;
import kr.me.seesaw.request.SavePermissionRequest;
import kr.me.seesaw.response.PermissionResponse;
import kr.me.seesaw.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/permissions")
public class PermissionApiController {

    private final PermissionService permissionService;

    @GetMapping
    public ResponseEntity<Map<String, List<PermissionResponse>>> getPermissionsByTargetId(@RequestParam("targetId") String targetId) {
        List<PermissionResponse> permissions = permissionService.getPermissionsByTargetId(targetId);
        return ResponseEntity.ok(Map.of("permissions", permissions));
    }

    @PostMapping
    public ResponseEntity<Map<String, PermissionResponse>> savePermission(@Valid @RequestBody SavePermissionRequest command) {
        PermissionResponse permission = permissionService.savePermission(command);
        return ResponseEntity.ok(Map.of("permission", permission));
    }

}
