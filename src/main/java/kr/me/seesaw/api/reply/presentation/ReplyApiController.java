package kr.me.seesaw.api.reply.presentation;

import jakarta.validation.Valid;
import kr.me.seesaw.api.reply.dto.CreateReplyRequest;
import kr.me.seesaw.api.reply.dto.UpdateReplyRequest;
import kr.me.seesaw.core.support.message.CmsMessageSource;
import kr.me.seesaw.api.reply.dto.ReplyResponse;
import kr.me.seesaw.api.article.ArticleQueryService;
import kr.me.seesaw.api.reply.ReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/replies")
public class ReplyApiController {

    private final CmsMessageSource messageSource;

    private final ArticleQueryService articleQueryService;

    private final ReplyService replyService;

    @GetMapping
    public ResponseEntity<Map<String, List<ReplyResponse>>> getRepliesByArticleId(@RequestParam("articleId") String articleId) {
        List<ReplyResponse> replies = articleQueryService.getReplies(articleId);
        return ResponseEntity.ok(Map.of("replies", replies));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> create(@Valid CreateReplyRequest command) {
        replyService.create(command);
        String message = messageSource.getMessage("command.success.create");
        return ResponseEntity.ok(message);
    }

    @PreAuthorize("@replyPermissionService.isOwner(#id)")
    @PostMapping(value = "/{id}", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> update(@PathVariable("id") String id, @Valid UpdateReplyRequest command) {
        replyService.update(id, command);
        String message = messageSource.getMessage("command.success.update");
        return ResponseEntity.ok(message);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @replyPermissionService.isOwner(#id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        replyService.delete(id);
        String message = messageSource.getMessage("command.success.delete");
        return ResponseEntity.ok(message);
    }

}
