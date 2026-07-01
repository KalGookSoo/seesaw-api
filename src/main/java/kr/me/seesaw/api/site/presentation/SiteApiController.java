package kr.me.seesaw.api.site.presentation;

import jakarta.validation.Valid;
import kr.me.seesaw.api.site.dto.CreateSiteRequest;
import kr.me.seesaw.core.support.authentication.PrincipalProvider;
import kr.me.seesaw.api.site.dto.SiteResponse;
import kr.me.seesaw.api.site.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sites")
public class SiteApiController {

    private final SiteService siteService;

    private final PrincipalProvider principalProvider;

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping
    public ResponseEntity<List<SiteResponse>> getOwnSites() {
        String username = principalProvider.getAuthentication().getName();
        List<SiteResponse> sites = siteService.getOwnSites(username);
        return ResponseEntity.ok(sites);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    public ResponseEntity<SiteResponse> createSite(@Valid CreateSiteRequest command) throws IOException {
        SiteResponse site = siteService.createSite(command);
        return ResponseEntity.ok(site);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<SiteResponse> getSiteById(@PathVariable String id) {
        SiteResponse site = siteService.getSiteById(id);
        return ResponseEntity.ok(site);
    }

    @GetMapping("/by-domain/{domainName}")
    public ResponseEntity<SiteResponse> getSiteContext(@PathVariable("domainName") String domainName) {
        SiteResponse site = siteService.getSiteByDomainName(domainName);
        return ResponseEntity.ok(site);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/{id}")
    public ResponseEntity<SiteResponse> updateSite(@PathVariable String id, @Valid CreateSiteRequest command) throws IOException {
        SiteResponse site = siteService.updateSite(id, command);
        return ResponseEntity.ok(site);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSite(@PathVariable String id) {
        siteService.deleteSite(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
