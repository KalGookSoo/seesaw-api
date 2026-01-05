package kr.me.seesaw.controller;

import kr.me.seesaw.model.PermissionModel;
import kr.me.seesaw.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/permissions")
public class PermissionApiController {

    private final PermissionService permissionService;

    @GetMapping
    public ResponseEntity<Map<String, List<PermissionModel>>> getPermissionsByTargetId(@RequestParam("targetId") String targetId) {
        List<PermissionModel> permissions = permissionService.getPermissionsByTargetId(targetId);
        return ResponseEntity.ok(Map.of("permissions", permissions));
    }

}
