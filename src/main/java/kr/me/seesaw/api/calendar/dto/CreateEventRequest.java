package kr.me.seesaw.api.calendar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.me.seesaw.core.domain.event.EventStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@Schema(name = "CreateEventRequest", description = "일정 생성 커맨드")
public class CreateEventRequest {

    private static final long MAX_FILE_SIZE = 50L * 1024L * 1024L;

    private static final long MAX_VIDEO_FILE_SIZE = 200L * 1024L * 1024L;

    private static final Set<String> ALLOWED_VIDEO_MIME_TYPES = Set.of("video/mp4", "video/webm", "video/quicktime");

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
    @NotBlank
    private String content;

    @Schema(description = "장소")
    private String location;

    @Schema(description = "상세 장소")
    @Size(max = 255)
    private String locationDetail;

    @Schema(description = "상태")
    private EventStatus status = EventStatus.CONFIRMED;

    @Schema(description = "시간대 식별자")
    private String tzid = "Asia/Seoul";

    @Schema(description = "카테고리 ID")
    @NotBlank
    private String categoryId;

    @Schema(description = "첨부파일")
    private List<MultipartFile> multipartFiles = new ArrayList<>();

    @Schema(description = "이미지")
    private List<MultipartFile> inlineImages = new ArrayList<>();

    @Schema(description = "동영상")
    private List<MultipartFile> inlineVideos = new ArrayList<>();

    @AssertTrue(message = "첨부파일은 최대 50MB까지 업로드할 수 있습니다.")
    public boolean isMultipartFilesSizeValid() {
        return isMultipartFilesSizeValid(multipartFiles);
    }

    @AssertTrue(message = "이미지는 최대 50MB까지 업로드할 수 있습니다.")
    public boolean isInlineImagesSizeValid() {
        return isMultipartFilesSizeValid(inlineImages);
    }

    @AssertTrue(message = "동영상은 최대 200MB까지 업로드할 수 있습니다.")
    public boolean isInlineVideosSizeValid() {
        return inlineVideos == null || inlineVideos.stream()
                .allMatch(multipartFile -> multipartFile == null || multipartFile.isEmpty() || multipartFile.getSize() <= MAX_VIDEO_FILE_SIZE);
    }

    @AssertTrue(message = "동영상은 mp4, webm, mov 포맷만 업로드할 수 있습니다.")
    public boolean isInlineVideosFormatValid() {
        return inlineVideos == null || inlineVideos.stream()
                .allMatch(multipartFile -> multipartFile == null || multipartFile.isEmpty() || ALLOWED_VIDEO_MIME_TYPES.contains(multipartFile.getContentType()));
    }

    private boolean isMultipartFilesSizeValid(List<MultipartFile> multipartFiles) {
        return multipartFiles == null || multipartFiles.stream()
                .allMatch(this::isMultipartFileSizeValid);
    }

    private boolean isMultipartFileSizeValid(MultipartFile multipartFile) {
        return multipartFile == null || multipartFile.isEmpty() || multipartFile.getSize() <= MAX_FILE_SIZE;
    }

}
