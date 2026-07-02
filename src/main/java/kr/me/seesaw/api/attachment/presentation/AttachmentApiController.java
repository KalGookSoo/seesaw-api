package kr.me.seesaw.api.attachment.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import kr.me.seesaw.core.support.file.FileManager;
import kr.me.seesaw.api.attachment.dto.AttachmentResponse;
import kr.me.seesaw.api.attachment.AttachmentQueryService;
import kr.me.seesaw.api.attachment.AttachmentService;
import kr.me.seesaw.core.support.pattern.PatternMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/api/attachments")
public class AttachmentApiController {

    private final AttachmentService attachmentService;

    private final AttachmentQueryService attachmentQueryService;

    private final FileManager fileManager;

    @GetMapping
    public ResponseEntity<Map<String, List<AttachmentResponse>>> getAttachments(@RequestParam("referenceId") @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String referenceId) {
        List<AttachmentResponse> attachments = attachmentQueryService.getAttachments(referenceId);
        return ResponseEntity.ok(Map.of("attachments", attachments));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getAttachment(
            @PathVariable("id") @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String id
    ) throws IOException {
        AttachmentResponse attachment = attachmentService.getAttachmentById(id);
        ByteArrayInputStream stream = fileManager.read(attachmentService.getAbsolutePath(attachment.getPathName(), attachment.getName()));
        InputStreamResource resource = new InputStreamResource(stream);
        String fileName = attachment.getOriginalName();
        HttpHeaders headers = new HttpHeaders();
        String contentType = attachment.getMimeType();

        if (attachment.isPreviewable() && !contentType.startsWith("image/") && !MediaType.APPLICATION_PDF_VALUE.equals(contentType)) {
            headers.add("Content-Security-Policy", "sandbox; default-src 'none'; style-src 'unsafe-inline';");
            headers.add("X-Content-Type-Options", "nosniff");
            headers.add("X-XSS-Protection", "1; mode=block");

            if (!MediaType.TEXT_HTML_VALUE.equals(contentType)) {
                contentType = MediaType.TEXT_PLAIN_VALUE;
            }
        }

        headers.add(HttpHeaders.CONTENT_TYPE, contentType);

        ContentDisposition.Builder builder = attachment.isPreviewable() ? ContentDisposition.inline() : ContentDisposition.attachment();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, builder
                .filename(fileName, StandardCharsets.UTF_8)
                .build()
                .toString());
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(resource);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @defaultAttachmentPermissionService.isOwner(#id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable("id") @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String id) {
        attachmentService.deleteAttachment(id);
        return ResponseEntity.noContent().build();
    }

}
