package kr.me.seesaw.api.attachment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.me.seesaw.core.domain.attachment.Attachment;
import kr.me.seesaw.core.support.dto.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Schema(name = "AttachmentResponse", description = "첨부파일 모델")
@ToString
@EqualsAndHashCode(callSuper = true)
@Getter
@AllArgsConstructor
public final class AttachmentResponse extends BaseResponse {

    @Schema(description = "참조 식별자", example = "8f14e45f-ea9d-4b1c-a3a4-12c4b2a9c001")
    private String referenceId;

    @Schema(description = "", example = "")
    private String originalName;

    @Schema(description = "", example = "")
    private String name;

    @Schema(description = "", example = "")
    private String pathName;

    @Schema(description = "", example = "")
    private String mimeType;

    @Schema(description = "", example = "")
    private long size;

    public AttachmentResponse(Attachment attachment) {
        setBaseModel(attachment);
        this.referenceId = attachment.getReferenceId();
        this.originalName = attachment.getOriginalName();
        this.name = attachment.getName();
        this.pathName = attachment.getPathName();
        this.mimeType = attachment.getMimeType();
        this.size = attachment.getSize();
    }

    public boolean isInlineImage() {
        return Attachment.Type.INLINE_IMAGE.getPath().equals(pathName);
    }

    public boolean isAttachment() {
        return Attachment.Type.ATTACHMENT.getPath().equals(pathName);
    }

    public boolean isPreviewable() {
        if (mimeType == null) return false;
        return mimeType.startsWith("image/") || 
               mimeType.startsWith("text/") || 
               "application/pdf".equals(mimeType) ||
               "application/json".equals(mimeType) ||
               "application/xml".equals(mimeType) ||
               "application/javascript".equals(mimeType);
    }

    public String getIconClass() {
        if (mimeType == null) return "bi-file-earmark";
        if (mimeType.startsWith("image/")) return "bi-file-earmark-image";
        if (mimeType.startsWith("video/")) return "bi-file-earmark-play";
        if (mimeType.startsWith("audio/")) return "bi-file-earmark-music";
        if ("application/pdf".equals(mimeType)) return "bi-file-earmark-pdf";
        if ("text/html".equals(mimeType)) return "bi-filetype-html";
        if ("text/css".equals(mimeType)) return "bi-filetype-css";
        if (mimeType.contains("javascript")) return "bi-filetype-js";
        if (mimeType.contains("json")) return "bi-filetype-json";
        if (mimeType.contains("xml")) return "bi-filetype-xml";
        if ("text/plain".equals(mimeType)) return "bi-filetype-txt";
        if (mimeType.contains("msword") || mimeType.contains("wordprocessingml")) return "bi-file-earmark-word";
        if (mimeType.contains("ms-excel") || mimeType.contains("spreadsheetml")) return "bi-file-earmark-excel";
        if (mimeType.contains("ms-powerpoint") || mimeType.contains("presentationml")) return "bi-file-earmark-ppt";
        if (mimeType.contains("zip") || mimeType.contains("archive") || mimeType.contains("compressed")) return "bi-file-earmark-zip";
        if (mimeType.startsWith("text/")) return "bi-file-earmark-text";
        return "bi-file-earmark";
    }

    public String getFormattedSize() {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        return String.format("%.1f MB", size / (1024.0 * 1024.0));
    }

}
