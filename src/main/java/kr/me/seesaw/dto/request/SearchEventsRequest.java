package kr.me.seesaw.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Builder
@Schema(name = "SearchEventsRequest", description = "일정 목록 검색 요청")
public record SearchEventsRequest(
        @Schema(description = "카테고리 식별자") String categoryId,
        @Schema(description = "검색 시작 일시") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
        @Schema(description = "검색 종료 일시") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
        @Schema(description = "검색어 (제목/본문)") String query
) {

}

