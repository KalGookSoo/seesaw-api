package kr.me.seesaw.api.rolemapping.presentation;

import kr.me.seesaw.api.rolemapping.RoleMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/role-mappings")
public class RoleMappingApiController {

    private final RoleMappingService roleMappingService;

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/me")
    public ResponseEntity<Void> removeMyRoleMapping() {
        roleMappingService.removeMyRoleMapping();
        return ResponseEntity.noContent().build();
    }

}
