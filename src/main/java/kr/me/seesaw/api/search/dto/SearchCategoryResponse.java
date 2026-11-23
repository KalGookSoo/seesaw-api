package kr.me.seesaw.api.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.me.seesaw.core.domain.category.CategoryType;

@Schema(name = "SearchCategoryResponse", description = "통합 검색 카테고리 결과")
public record SearchCategoryResponse(
        @Schema(description = "카테고리 식별자") String id,
        @Schema(description = "카테고리 이름") String name,
        @Schema(description = "카테고리 설명") String description,
        @Schema(description = "카테고리 타입") CategoryType type,
        @Schema(description = "카테고리 바로가기") String url
) {
}
