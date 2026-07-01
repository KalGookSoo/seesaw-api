package kr.me.seesaw.api.category.dto;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.me.seesaw.api.article.dto.ArticleResponse;
import kr.me.seesaw.core.support.dto.AbstractHierarchicalResponse;
import kr.me.seesaw.core.support.hierarchy.Hierarchical;
import kr.me.seesaw.core.domain.category.Category;
import kr.me.seesaw.core.domain.category.CategoryType;
import lombok.*;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;

@Schema(name = "CategoryResponse", description = "카테고리 모델")
@ToString(exclude = {"articles", "recentArticles"})
@EqualsAndHashCode(exclude = {"articles", "recentArticles"}, callSuper = true)
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor
public final class CategoryResponse extends AbstractHierarchicalResponse<CategoryResponse> implements Hierarchical<CategoryResponse, String> {

    @JsonManagedReference
    private final List<ArticleResponse> articles = new ArrayList<>();

    @Schema(description = "카테고리 이름", example = "공지사항")
    private String name;

    @Schema(description = "카테고리 설명", example = "중요 공지 모음")
    private String description;

    @Schema(description = "카테고리 타입", example = "BOARD", implementation = CategoryType.class)
    private CategoryType type;

    @Schema(description = "사이트 노출 여부", example = "true")
    private boolean siteExposed;

    @Schema(description = "사이트 노출 순서", example = "1")
    private int siteExposedOrder;

    @Schema(description = "노출 여부", example = "true")
    private boolean exposed;

    @Schema(description = "정렬 순서", example = "10")
    private Integer sequence;

    @Schema(description = "사이트 식별자(UUID)", example = "8f14e45f-ea9d-4b1c-a3a4-12c4b2a9c001")
    private String siteId;

    @JsonManagedReference
    private List<ArticleResponse> recentArticles = new ArrayList<>();

    public CategoryResponse(Category category) {
        setBaseModel(category);
        setParentId(category.getParentId());
        name = category.getName();
        description = category.getDescription();
        type = category.getType();
        siteExposed = category.isSiteExposed();
        siteExposedOrder = category.getSiteExposedOrder();
        exposed = category.isExposed();
        sequence = category.getSequence();
        siteId = category.getSite().getId();
    }

    @Override
    public void addChild(@NonNull CategoryResponse child) {
        getChildren().add(child);
        child.setParentId(getId());
        child.setParent(this);
    }

    public void joinArticles(List<ArticleResponse> articles) {
        articles.stream().filter(this::isArticleForCategory).forEach(this::addArticle);
    }

    private boolean isArticleForCategory(ArticleResponse article) {
        return getId().equals(article.getCategoryId());
    }

    public void addArticle(ArticleResponse article) {
        articles.add(article);
    }

    public void addRecentArticle(ArticleResponse article) {
        recentArticles.add(article);
    }

    @Schema(description = "카테고리 링크 URL")
    public String getUrl() {
        if (type != CategoryType.NONE) {
            return String.format("/articles?categoryType=%s&categoryId=%s", type, getId());
        }
        if (getChildren() != null && !getChildren().isEmpty()) {
            CategoryResponse firstChild = getChildren().get(0);
            return String.format("/articles?categoryType=%s&categoryId=%s", firstChild.getType(), firstChild.getId());
        }
        return "#";
    }

}
