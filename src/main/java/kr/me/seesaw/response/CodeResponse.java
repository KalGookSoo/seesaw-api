package kr.me.seesaw.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.me.seesaw.core.hierarchy.Hierarchical;
import kr.me.seesaw.domain.Code;
import lombok.*;
import org.hibernate.annotations.Comment;

@Schema(name = "CodeResponse", description = "코드 모델")
@ToString
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class CodeResponse extends AbstractHierarchicalResponse<CodeResponse> implements Hierarchical<CodeResponse, String> {

    @Comment("이름")
    @Schema(description = "코드 이름")
    private String name;

    @Comment("설명")
    @Schema(description = "코드 설명")
    private String description;

    @Comment("순서")
    @Schema(description = "정렬 순서")
    private Integer sequence;

    public CodeResponse(Code code) {
        setBaseModel(code);
        setParentId(code.getParentId());
        this.name = code.getName();
        this.description = code.getDescription();
        this.sequence = code.getSequence();
    }

    @Override
    public void addChild(CodeResponse child) {
        getChildren().add(child);
        child.setParentId(getId());
        child.setParent(this);
    }

}
