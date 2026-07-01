package kr.me.seesaw.api.role.presentation;

import kr.me.seesaw.api.role.dto.RoleResponse;
import kr.me.seesaw.api.role.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/roles")
public class RoleApiController {

    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<Map<String, List<RoleResponse>>> getAllRoles() {
        List<RoleResponse> roles = roleService.getAllRoles();
        return ResponseEntity.ok(Map.of("roles", roles));
    }

}
