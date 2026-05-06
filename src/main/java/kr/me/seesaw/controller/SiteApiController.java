package kr.me.seesaw.controller;

import jakarta.validation.Valid;
import kr.me.seesaw.command.CreateSiteCommand;
import kr.me.seesaw.core.authentication.PrincipalProvider;
import kr.me.seesaw.model.SiteModel;
import kr.me.seesaw.service.SiteService;
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
    public ResponseEntity<List<SiteModel>> getOwnSites() {
        String username = principalProvider.getAuthentication().getName();
        List<SiteModel> sites = siteService.getOwnSites(username);
        return ResponseEntity.ok(sites);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    public ResponseEntity<SiteModel> createSite(@Valid CreateSiteCommand command) throws IOException {
        SiteModel site = siteService.createSite(command);
        return ResponseEntity.ok(site);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<SiteModel> getSiteById(@PathVariable String id) {
        SiteModel site = siteService.getSiteById(id);
        return ResponseEntity.ok(site);
    }

    @GetMapping("/by-domain/{domainName}")
    public ResponseEntity<SiteModel> getSiteContext(@PathVariable("domainName") String domainName) {
        SiteModel site = siteService.getSiteByDomainName(domainName);
        return ResponseEntity.ok(site);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/{id}")
    public ResponseEntity<SiteModel> updateSite(@PathVariable String id, @Valid CreateSiteCommand command) throws IOException {
        SiteModel site = siteService.updateSite(id, command);
        return ResponseEntity.ok(site);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSite(@PathVariable String id) {
        siteService.deleteSite(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
