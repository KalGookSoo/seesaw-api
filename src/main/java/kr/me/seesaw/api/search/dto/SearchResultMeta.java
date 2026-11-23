package kr.me.seesaw.api.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SearchResultMeta", description = "검색 결과 페이지 메타데이터")
public record SearchResultMeta(
        @Schema(description = "현재 페이지") int page,
        @Schema(description = "페이지 크기") int limit,
        @Schema(description = "카테고리 전체 결과 수") long categoryTotalCount,
        @Schema(description = "게시글 전체 결과 수") long articleTotalCount,
        @Schema(description = "카테고리 다음 페이지 존재 여부") boolean categoryHasMore,
        @Schema(description = "게시글 다음 페이지 존재 여부") boolean articleHasMore
) {
}
