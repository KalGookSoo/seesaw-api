package kr.me.seesaw.api.vote.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.me.seesaw.core.domain.vote.Vote;
import kr.me.seesaw.core.support.dto.BaseResponse;
import lombok.*;
import org.hibernate.annotations.Comment;

@Schema(name = "VoteResponse", description = "투표 모델")
@ToString
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class VoteResponse extends BaseResponse {

    @Comment("참조 식별자")
    @Schema(description = "참조 식별자(UUID)")
    private String referenceId;

    @Comment("찬성여부")
    @Schema(description = "찬성 여부")
    private boolean approved;

    public VoteResponse(Vote vote) {
        setBaseModel(vote);
        this.referenceId = vote.getReferenceId();
        this.approved = vote.isApproved();
    }
}
