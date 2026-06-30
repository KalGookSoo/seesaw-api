package kr.me.seesaw.request;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "게시글 이동 커맨드")
public record MoveArticleRequest(
        @Parameter(description = "대상 카테고리 식별자", required = true)
        @Schema(description = "대상 카테고리 식별자", example = "8f14e45f-ea9d-4b1c-a3a4-12c4b2a9c001")
        @NotBlank
        @NotNull
        String categoryId
) {

}
