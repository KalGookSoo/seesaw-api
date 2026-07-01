package kr.me.seesaw.api.reply.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "댓글 수정 커맨드")
@Data
public class UpdateReplyRequest implements Serializable {

    private static final long MAX_FILE_SIZE = 50L * 1024L * 1024L;

    @Parameter(description = "노출 여부")
    @Schema(description = "노출 여부", example = "true")
    private boolean exposed;

    @Parameter(description = "내용", required = true)
    @Schema(description = "내용", example = "내용")
    @NotNull
    @NotBlank
    private String content;

    @Parameter(description = "첨부파일")
    @Schema(description = "첨부파일", example = "첨부파일")
    private List<MultipartFile> multipartFiles = new ArrayList<>();

    @AssertTrue(message = "첨부파일은 최대 50MB까지 업로드할 수 있습니다.")
    public boolean isMultipartFilesSizeValid() {
        return multipartFiles == null || multipartFiles.stream()
                .allMatch(this::isMultipartFileSizeValid);
    }

    private boolean isMultipartFileSizeValid(MultipartFile multipartFile) {
        return multipartFile == null || multipartFile.isEmpty() || multipartFile.getSize() <= MAX_FILE_SIZE;
    }

}
