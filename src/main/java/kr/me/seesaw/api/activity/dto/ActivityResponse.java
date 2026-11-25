package kr.me.seesaw.api.activity.dto;

import java.time.LocalDateTime;

public record ActivityResponse(
        String id, String type, String articleId, String title, String preview,
        String categoryId, String categoryName, String articleType, LocalDateTime createdDate
) {
}
