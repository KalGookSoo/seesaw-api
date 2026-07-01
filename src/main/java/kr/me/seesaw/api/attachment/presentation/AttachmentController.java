package kr.me.seesaw.api.attachment.presentation;

import jakarta.servlet.http.HttpServletResponse;
import kr.me.seesaw.api.article.ArticleQueryService;
import kr.me.seesaw.api.attachment.AttachmentService;
import kr.me.seesaw.api.attachment.dto.AttachmentResponse;
import kr.me.seesaw.core.support.file.FileManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RequiredArgsConstructor
@Controller
@RequestMapping("/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    private final ArticleQueryService articleQueryService;

    private final FileManager fileManager;

    @GetMapping("/{id}/download")
    public void getAttachment(
            @PathVariable("id") String id,
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
    public void getAttachments(
            @RequestParam String articleId,
            HttpServletResponse response
    ) throws IOException {
        List<AttachmentResponse> attachments = articleQueryService.getAttachments(articleId)
                .stream()
                .filter(AttachmentResponse::isAttachment)
                .toList();
        if (attachments.isEmpty()) {
            throw new NoSuchElementException();
        }
        if (attachments.size() == 1) {
            getAttachment(attachments.get(0).getId(), response);
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

}
