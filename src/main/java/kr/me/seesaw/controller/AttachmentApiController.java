package kr.me.seesaw.controller;

import kr.me.seesaw.core.file.FileManager;
import kr.me.seesaw.model.AttachmentModel;
import kr.me.seesaw.service.AttachmentQueryService;
import kr.me.seesaw.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/attachments")
public class AttachmentApiController {

    private final AttachmentService attachmentService;

    private final AttachmentQueryService attachmentQueryService;

    private final FileManager fileManager;

    @GetMapping
    public ResponseEntity<Map<String, List<AttachmentModel>>> getAttachments(@RequestParam("referenceId") String referenceId) {
        List<AttachmentModel> attachments = attachmentQueryService.getAttachments(referenceId);
        return ResponseEntity.ok(Map.of("attachments", attachments));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getAttachment(
            @PathVariable("id") String id
    ) throws IOException {
        AttachmentModel attachment = attachmentService.getAttachmentById(id);
        ByteArrayInputStream stream = fileManager.read(attachmentService.getAbsolutePath(attachment.getPathName(), attachment.getName()));
        InputStreamResource resource = new InputStreamResource(stream);
        String fileName = attachment.getOriginalName();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, attachment.getMimeType());
        headers.add(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                .filename(fileName, StandardCharsets.UTF_8)
                .build()
                .toString());
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(resource);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @defaultAttachmentPermissionService.isOwner(#id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable("id") String id) {
        attachmentService.deleteAttachment(id);
        return ResponseEntity.noContent().build();
    }

}
