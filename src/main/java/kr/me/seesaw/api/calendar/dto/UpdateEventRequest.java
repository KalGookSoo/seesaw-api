package kr.me.seesaw.api.calendar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.me.seesaw.core.domain.event.EventStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(name = "UpdateEventRequest", description = "일정 수정 커맨드")
public class UpdateEventRequest {

    private static final long MAX_FILE_SIZE = 50L * 1024L * 1024L;

    @Schema(description = "시작 일시")
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dtStart;

    @Schema(description = "종료 일시")
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dtEnd;

    @Schema(description = "제목")
    @NotBlank
    private String title;

    @Schema(description = "상세 설명")
    private String content;

    @Schema(description = "장소")
    private String location;

    @Schema(description = "상태")
    private EventStatus status;

    @Schema(description = "시간대 식별자")
    private String tzid;

    @Schema(description = "카테고리 ID")
    private String categoryId;

    @Schema(description = "첨부파일")
    private List<MultipartFile> multipartFiles = new ArrayList<>();

    @Schema(description = "이미지")
    private List<MultipartFile> inlineImages = new ArrayList<>();

    @AssertTrue(message = "첨부파일은 최대 50MB까지 업로드할 수 있습니다.")
    public boolean isMultipartFilesSizeValid() {
        return isMultipartFilesSizeValid(multipartFiles);
    }

    @AssertTrue(message = "이미지는 최대 50MB까지 업로드할 수 있습니다.")
    public boolean isInlineImagesSizeValid() {
        return isMultipartFilesSizeValid(inlineImages);
    }

    private boolean isMultipartFilesSizeValid(List<MultipartFile> multipartFiles) {
        return multipartFiles == null || multipartFiles.stream()
                .allMatch(this::isMultipartFileSizeValid);
    }

    private boolean isMultipartFileSizeValid(MultipartFile multipartFile) {
        return multipartFile == null || multipartFile.isEmpty() || multipartFile.getSize() <= MAX_FILE_SIZE;
    }

}
