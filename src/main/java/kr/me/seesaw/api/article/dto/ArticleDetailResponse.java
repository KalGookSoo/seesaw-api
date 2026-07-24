package kr.me.seesaw.api.article.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.springframework.lang.Nullable;

@Schema(name = "ArticleDetailResponse", description = "게시글 상세 조회 응답")
@Builder
public record ArticleDetailResponse(
        @Schema(description = "게시글 상세 정보")
        ArticleResponse article,
        @Nullable
        @Schema(description = "이전 게시글 식별자", example = "8f14e45f-ea9d-4b1c-a3a4-12c4b2a9c001")
        String previousArticleId,
        @Nullable
        @Schema(description = "다음 게시글 식별자", example = "8f14e45f-ea9d-4b1c-a3a4-12c4b2a9c001")
        String nextArticleId
) {

}
