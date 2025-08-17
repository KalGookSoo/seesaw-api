package kr.me.seesaw.controller;

import jakarta.validation.Valid;
import kr.me.seesaw.command.CreateSiteCommand;
import kr.me.seesaw.core.authentication.PrincipalProvider;
import kr.me.seesaw.domain.Site;
import kr.me.seesaw.service.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @PreAuthorize("isAuthenticated() and hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    public ResponseEntity<Map<String, Site>> createSite(@RequestBody @Valid CreateSiteCommand command) {
        Site site = siteService.createSite(command);
        return ResponseEntity.ok(Map.of("site", site));
    }

    @PreAuthorize("isAuthenticated() and hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<Site> getSite(@PathVariable String id) {
        Site site = siteService.getSite(id);
        return ResponseEntity.ok(site);
    }

    @PreAuthorize("isAuthenticated() and hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<Site> updateSite(@PathVariable String id, @RequestBody @Valid CreateSiteCommand command) {
        Site site = siteService.updateSite(id, command);
        return ResponseEntity.ok(site);
    }

    @PreAuthorize("isAuthenticated() and hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSite(@PathVariable String id) {
        siteService.deleteSite(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
