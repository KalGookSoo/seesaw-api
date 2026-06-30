package kr.me.seesaw.api.framework.controller;

import jakarta.validation.Valid;
import kr.me.seesaw.request.CreateReplyRequest;
import kr.me.seesaw.request.UpdateReplyRequest;
import kr.me.seesaw.message.CmsMessageSource;
import kr.me.seesaw.service.ReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/replies")
public class ReplyApiController {

    private final CmsMessageSource messageSource;

    private final ReplyService articleService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping(produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> create(@Valid CreateReplyRequest command) {
        articleService.create(command);
        String message = messageSource.getMessage("command.success.create");
        return ResponseEntity.ok(message);
    }

    @PreAuthorize("@replyPermissionService.isOwner(#id)")
    @PostMapping(value = "/{id}", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> update(@PathVariable("id") String id, @Valid UpdateReplyRequest command) {
        articleService.update(id, command);
        String message = messageSource.getMessage("command.success.update");
        return ResponseEntity.ok(message);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @replyPermissionService.isOwner(#id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        articleService.delete(id);
        String message = messageSource.getMessage("command.success.delete");
        return ResponseEntity.ok(message);
    }

}
