package kr.me.seesaw.api.attachment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import kr.me.seesaw.core.domain.attachment.Attachment;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Schema(description = "첨부파일 생성 커맨드")
@ToString
@AllArgsConstructor
@EqualsAndHashCode
@Getter
public final class CreateAttachmentRequest implements Serializable {

    private static final long MAX_FILE_SIZE = 50L * 1024L * 1024L;

    private final String referenceId;

    private final Attachment.Type type;

    private final MultipartFile multipartFile;

    @AssertTrue(message = "첨부파일은 최대 50MB까지 업로드할 수 있습니다.")
    public boolean isMultipartFileSizeValid() {
        return multipartFile == null || multipartFile.isEmpty() || multipartFile.getSize() <= MAX_FILE_SIZE;
    }

}
