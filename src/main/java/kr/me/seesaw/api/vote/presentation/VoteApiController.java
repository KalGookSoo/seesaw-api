package kr.me.seesaw.api.vote.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import kr.me.seesaw.api.vote.VoteService;
import kr.me.seesaw.api.vote.dto.VoteSummaryResponse;
import kr.me.seesaw.core.support.pattern.PatternMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/api/votes")
public class VoteApiController {

    private final VoteService voteService;

    @GetMapping
    public ResponseEntity<VoteSummaryResponse> getSummary(
            @RequestParam @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String referenceId
    ) {
        return ResponseEntity.ok(voteService.getSummary(referenceId));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{referenceId}")
    public ResponseEntity<VoteSummaryResponse> like(
            @PathVariable @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String referenceId
    ) {
        return ResponseEntity.ok(voteService.like(referenceId));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{referenceId}")
    public ResponseEntity<VoteSummaryResponse> unlike(
            @PathVariable @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String referenceId
    ) {
        return ResponseEntity.ok(voteService.unlike(referenceId));
    }

}
