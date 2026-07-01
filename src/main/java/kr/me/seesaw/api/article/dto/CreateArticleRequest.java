package kr.me.seesaw.api.article.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.me.seesaw.core.domain.article.ArticleType;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Schema(description = "게시글 생성 커맨드")
@Data
public class CreateArticleRequest {

    private static final long MAX_FILE_SIZE = 50L * 1024L * 1024L;

    @Parameter(description = "카테고리 식별자", required = true)
    @Schema(description = "카테고리 식별자", example = "카테고리 식별자")
    @NotBlank
    @NotNull
    private String categoryId;

    @Parameter(description = "타입", required = true)
    @Schema(description = "타입", example = "타입")
    @NotNull
    private ArticleType type;

    @Parameter(description = "고정여부", required = true)
    @Schema(description = "고정여부", example = "고정여부")
    private boolean fixed;

    @Parameter(description = "고정순서", required = true)
    @Schema(description = "고정순서", example = "고정순서")
    private Integer fixedOrder;

    @Parameter(description = "제목", required = true)
    @Schema(description = "제목", example = "제목")
    @NotBlank
    @NotNull
    @Length(min = 1, max = 30)
    private String title;

    @Parameter(description = "본문", required = true)
    @Schema(description = "본문", example = "본문")
    @NotBlank
    @NotNull
    private String content;

    @Parameter(description = "첨부파일")
    @Schema(description = "첨부파일", example = "첨부파일")
    private List<MultipartFile> multipartFiles = new ArrayList<>();

    @Parameter(description = "이미지")
    @Schema(description = "이미지", example = "이미지")
    private List<MultipartFile> inlineImages = new ArrayList<>();

    public List<MultipartFile> getMultipartFiles() {
        return multipartFiles.stream()
                .filter(Predicate.not(MultipartFile::isEmpty))
                .toList();
    }

    public List<MultipartFile> getInlineImages() {
        return inlineImages.stream()
                .filter(Predicate.not(MultipartFile::isEmpty))
                .toList();
    }

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
