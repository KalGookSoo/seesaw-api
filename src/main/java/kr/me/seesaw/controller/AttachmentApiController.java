package kr.me.seesaw.controller;

import kr.me.seesaw.core.file.FileIOService;
import kr.me.seesaw.domain.Attachment;
import kr.me.seesaw.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/attachments")
public class AttachmentApiController {

    private final AttachmentService attachmentService;

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getAttachment(
            @PathVariable("id") String id,
            @RequestHeader(HttpHeaders.USER_AGENT) String userAgent
    ) throws IOException {
        Attachment attachment = attachmentService.find(id);
        ByteArrayInputStream stream = FileIOService.read(attachmentService.getAbsolutePath(id));
        InputStreamResource resource = new InputStreamResource(stream);
        String fileName = attachment.getOriginalName();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, attachment.getMimeType());
        headers.add(HttpHeaders.CONTENT_DISPOSITION, getContentDisposition(userAgent, fileName));
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(resource);
    }

    private String getContentDisposition(String userAgent, String fileName) {
        String disposition = "attachment;filename=";
        if (userAgent.contains("MSIE")) {
            int i = userAgent.indexOf('M', 2);
            String IEV = userAgent.substring(i + 5, i + 8);
            disposition = IEV.equalsIgnoreCase("5.5") ? "filename=" : disposition;
        }
        return disposition + fileName;
    }
}
