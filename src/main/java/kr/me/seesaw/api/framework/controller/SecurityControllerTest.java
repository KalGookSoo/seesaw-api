package kr.me.seesaw.api.framework.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Profile("test")
@RequestMapping("/security-test")
@RestController
public class SecurityControllerTest {

    @PreAuthorize("isAnonymous()")
    @GetMapping("/anonymous")
    public ResponseEntity<Map<String, String>> anonymous() {
        return ResponseEntity.ok(Map.of("message", "anonymous"));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/authenticated")
    public ResponseEntity<Map<String, String>> authenticated() {
        return ResponseEntity.ok(Map.of("message", "authenticated"));
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<Map<String, String>> admin() {
        return ResponseEntity.ok(Map.of("message", "admin"));
    }

    @PreAuthorize("hasAnyRole('MANAGER')")
    @GetMapping("/manager")
    public ResponseEntity<Map<String, String>> manager() {
        return ResponseEntity.ok(Map.of("message", "manager"));
    }

    @PreAuthorize("hasAnyRole('USER')")
    @GetMapping("/user")
    public ResponseEntity<Map<String, String>> user() {
        return ResponseEntity.ok(Map.of("message", "user"));
    }

}
