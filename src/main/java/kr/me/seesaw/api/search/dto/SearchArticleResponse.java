package kr.me.seesaw.api.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(name = "SearchArticleResponse", description = "통합 검색 게시글 결과")
public record SearchArticleResponse(
        @Schema(description = "게시글 식별자") String id,
        @Schema(description = "게시글 제목") String title,
        @Schema(description = "본문 미리보기") String preview,
        @Schema(description = "카테고리 식별자") String categoryId,
        @Schema(description = "작성일시") LocalDateTime createdDate,
        @Schema(description = "게시글 바로가기") String url
) {
}
