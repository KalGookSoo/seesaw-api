package kr.me.seesaw.response;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.me.seesaw.domain.View;
import lombok.*;
import org.hibernate.annotations.Comment;

@Schema(name = "ViewResponse", description = "뷰 모델")
@ToString(exclude = {"article"})
@EqualsAndHashCode(exclude = {"article"}, callSuper = true)
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class ViewResponse extends BaseResponse {

    @Comment("게시글 식별자")
    @Schema(description = "게시글 식별자(UUID)")
    private String articleId;

    @JsonBackReference
    private ArticleResponse article;

    public ViewResponse(View view) {
        setBaseModel(view);
        this.articleId = (view.getArticle() != null ? view.getArticle().getId() : null);
    }
}
