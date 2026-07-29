package kr.me.seesaw.api.attachment.presentation;

import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StreamUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
            @PathVariable("id") @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String id,
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader
    ) throws IOException {
        AttachmentResponse attachment = attachmentService.getAttachmentById(id);
        String absolutePath = attachmentService.getAbsolutePath(attachment.getPathName(), attachment.getName());
        String fileName = attachment.getOriginalName();
        String contentType = attachment.getMimeType();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");

        if (attachment.isPreviewable()
                && !contentType.startsWith("image/")
                && !contentType.startsWith("video/")
                && !contentType.startsWith("audio/")
                && !MediaType.APPLICATION_PDF_VALUE.equals(contentType)) {
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

        List<HttpRange> ranges = List.of();
        if (rangeHeader != null) {
            try {
                ranges = HttpRange.parseRanges(rangeHeader);
            } catch (IllegalArgumentException ignored) {
                // 잘못된 Range 헤더는 무시하고 전체 응답으로 대체한다(RFC 7233).
            }
        }

        if (!ranges.isEmpty()) {
            long fileSize = attachment.getSize();
            HttpRange range = ranges.get(0);
            long start = range.getRangeStart(fileSize);
            long end = range.getRangeEnd(fileSize);
            long rangeLength = end - start + 1;

            InputStreamResource resource = new InputStreamResource(fileManager.read(absolutePath, start, rangeLength));
            headers.add(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize);
            headers.setContentLength(rangeLength);
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).headers(headers).body(resource);
        }

        InputStreamResource resource = new InputStreamResource(fileManager.read(absolutePath));
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(resource);
    }

    @GetMapping("/{id}/download")
    public void downloadAttachment(
            @PathVariable("id") @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String id,
            HttpServletResponse response
    ) throws IOException {
        AttachmentResponse attachment = attachmentService.getAttachmentById(id);
        String fileName = attachment.getOriginalName();
        ByteArrayInputStream inputStream = fileManager.read(attachmentService.getAbsolutePath(attachment.getPathName(), attachment.getName()));

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                .filename(fileName, StandardCharsets.UTF_8)
                .build()
                .toString());
        OutputStream outputStream = response.getOutputStream();
        FileCopyUtils.copy(inputStream, outputStream);
    }

    @GetMapping("/download-zip")
    public void downloadAttachments(
            @RequestParam @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String articleId,
            HttpServletResponse response
    ) throws IOException {
        List<AttachmentResponse> attachments = attachmentQueryService.getArticleAttachments(articleId);
        if (attachments.isEmpty()) {
            throw new NoSuchElementException();
        }
        if (attachments.size() == 1) {
            downloadAttachment(attachments.get(0).getId(), response);
            return;
        }
        String title = attachments.get(0).getOriginalName() + " (외 " + (attachments.size() - 1) + "개)";

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/zip");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                .filename(title + ".zip", StandardCharsets.UTF_8)
                .build()
                .toString());

        try (ZipOutputStream outputStream = new ZipOutputStream(response.getOutputStream())) {
            for (AttachmentResponse attachment : attachments) {
                ZipEntry entry = new ZipEntry(attachment.getOriginalName());
                outputStream.putNextEntry(entry);
                ByteArrayInputStream inputStream = fileManager.read(attachmentService.getAbsolutePath(attachment.getPathName(), attachment.getName()));
                StreamUtils.copy(inputStream, outputStream);
                outputStream.closeEntry();
            }
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @defaultAttachmentPermissionService.isOwner(#id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable("id") @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String id) {
        attachmentService.deleteAttachment(id);
        return ResponseEntity.noContent().build();
    }

}
