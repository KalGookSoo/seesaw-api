package kr.me.seesaw.api.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Schema(description = "메일 전송 커맨드")
@Builder
public record SendMailRequest(
        @NotBlank
        @Schema(description = "사이트 식별자", example = "8f14e45f-ea9d-4b1c-a3a4-12c4b2a9c001")
        String siteId,
        @NotBlank
        @Schema(description = "제목", example = "서비스 이용 문의")
        String title,
        @NotBlank
        @Schema(description = "본문", example = "안녕하세요. 문의드립니다.")
        String content
) {

}
