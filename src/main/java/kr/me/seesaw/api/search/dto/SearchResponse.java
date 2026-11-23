package kr.me.seesaw.api.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "SearchResponse", description = "통합 검색 결과")
public record SearchResponse(
        @Schema(description = "검색어") String keyword,
        @Schema(description = "카테고리 검색 결과") List<SearchCategoryResponse> categories,
        @Schema(description = "게시글 검색 결과") List<SearchArticleResponse> articles
) {
}
