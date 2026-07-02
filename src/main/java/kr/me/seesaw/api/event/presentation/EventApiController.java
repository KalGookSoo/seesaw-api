package kr.me.seesaw.api.event.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import kr.me.seesaw.api.calendar.EventWebService;
import kr.me.seesaw.api.calendar.dto.CreateEventRequest;
import kr.me.seesaw.api.calendar.dto.SearchEventsRequest;
import kr.me.seesaw.api.calendar.dto.UpdateEventRequest;
import kr.me.seesaw.api.calendar.dto.VEventResponse;
import kr.me.seesaw.core.support.pattern.PatternMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventApiController {

    private final EventWebService eventWebService;

    @GetMapping
    public ResponseEntity<List<VEventResponse>> findAll(@Valid SearchEventsRequest request) {
        return ResponseEntity.ok(eventWebService.findAll(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VEventResponse> find(@PathVariable @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String id) {
        return ResponseEntity.ok(eventWebService.find(id));
    }

    @PreAuthorize("isAuthenticated() and (hasAnyRole('ADMIN', 'MANAGER') or @categoryPermissionEvaluator.hasPermission(#command.categoryId, T(org.springframework.security.acls.domain.BasePermission).CREATE))")
    @PostMapping
    public ResponseEntity<VEventResponse> create(@Valid CreateEventRequest command) throws IOException {
        return ResponseEntity.ok(eventWebService.create(command));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<VEventResponse> update(
            @PathVariable @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String id,
            @Valid UpdateEventRequest command
    ) throws IOException {
        return ResponseEntity.ok(eventWebService.update(id, command));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @eventContext.isOwner(#id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String id) {
        eventWebService.delete(id);
        return ResponseEntity.ok().build();
    }

}
