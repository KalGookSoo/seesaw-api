package kr.me.seesaw.api.vote.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(name = "VoteSummaryResponse", description = "좋아요 집계 응답")
public record VoteSummaryResponse(
        @Schema(description = "게시글 또는 댓글 식별자(UUID)")
        String referenceId,
        @Schema(description = "좋아요 개수")
        long count,
        @Schema(description = "현재 사용자의 좋아요 여부")
        boolean liked
) {
}
