package kr.me.seesaw.api.article.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import kr.me.seesaw.core.domain.category.CategoryType;
import kr.me.seesaw.core.support.pattern.PatternMatcher;
import lombok.Builder;

@Schema(description = "게시글 상세 검색 조건")
@Builder
public record SearchArticleDetailRequest(
        @Parameter(description = "목록 카테고리 식별자")
        @Schema(description = "목록 카테고리 식별자", example = "8f14e45f-ea9d-4b1c-a3a4-12c4b2a9c001")
        @Pattern(regexp = PatternMatcher.UUID_V4)
        String categoryId,
        @Parameter(description = "카테고리 유형")
        @Schema(description = "카테고리 유형", implementation = CategoryType.class)
        CategoryType categoryType,
        @Parameter(description = "검색 대상 필드")
        @Schema(description = "검색 대상 필드", example = "title")
        String keyField,
        @Parameter(description = "검색어")
        @Schema(description = "검색어", example = "공지")
        String keyWord
) {

    public SearchArticlesRequest toSearchArticlesRequest() {
        SearchArticlesRequest search = new SearchArticlesRequest();
        search.setCategoryId(categoryId);
        search.setCategoryType(categoryType);
        search.setKeyField(keyField);
        search.setKeyWord(keyWord);
        return search;
    }

}
