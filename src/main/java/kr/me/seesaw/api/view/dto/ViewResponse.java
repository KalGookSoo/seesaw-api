package kr.me.seesaw.api.view.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.me.seesaw.core.domain.view.View;
import kr.me.seesaw.core.support.dto.BaseResponse;
import lombok.*;
import org.hibernate.annotations.Comment;

@Schema(name = "ViewResponse", description = "뷰 모델")
@ToString
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class ViewResponse extends BaseResponse {

    @Comment("게시글 식별자")
    @Schema(description = "게시글 식별자(UUID)")
    private String articleId;

    public ViewResponse(View view) {
        setBaseModel(view);
        this.articleId = view.getArticleId();
    }

}
