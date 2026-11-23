package kr.me.seesaw.api.notification.presentation;

import jakarta.validation.Valid;
import kr.me.seesaw.api.notification.HelpdeskService;
import kr.me.seesaw.api.notification.ReportService;
import kr.me.seesaw.api.notification.dto.SendMailRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/mail")
public class MailApiController {

    private final ReportService reportService;
    private final HelpdeskService helpdeskService;

    @PreAuthorize("isAuthenticated() and @siteAuthorizationService.hasRoleForSite(#command.siteId)")
    @PostMapping("/send-to-report")
    public ResponseEntity<Void> sendToReport(@Valid @RequestBody SendMailRequest command) {
        reportService.sendToReport(command.siteId(), command.title(), command.content());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated() and @siteAuthorizationService.hasRoleForSite(#command.siteId)")
    @PostMapping("/send-to-helpdesk")
    public ResponseEntity<Void> sendToHelpdesk(@Valid @RequestBody SendMailRequest command) {
        helpdeskService.sendToHelpdesk(command.siteId(), command.title(), command.content());
        return ResponseEntity.ok().build();
    }

}
