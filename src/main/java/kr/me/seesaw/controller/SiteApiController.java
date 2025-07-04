package kr.me.seesaw.controller;

import kr.me.seesaw.core.authentication.PrincipalProvider;
import kr.me.seesaw.domain.Site;
import kr.me.seesaw.service.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sites")
public class SiteApiController {

    private final SiteService siteService;

    private final PrincipalProvider principalProvider;

    @PreAuthorize("isAuthenticated() and hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping
    public ResponseEntity<List<Site>> getSites() {
        String username = principalProvider.getAuthentication().getName();
        List<Site> page = siteService.getOwnSites(username);
        return ResponseEntity.ok(page);
    }

}
