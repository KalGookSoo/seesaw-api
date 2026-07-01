package kr.me.seesaw.api.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.me.seesaw.core.domain.category.CategoryType;
import lombok.*;

import java.io.Serializable;

/**
 * 카테고리 생성 요청을 표현하는 커맨드 객체.
 */
@Schema(name = "CreateCategoryRequest", description = "카테고리 생성 커맨드")
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCategoryRequest implements Serializable {

    @NotNull
    @NotBlank
    @Schema(description = "카테고리 이름", example = "공지사항", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "카테고리 설명", example = "중요 공지 모음")
    private String description;

    @NotNull
    @Schema(description = "카테고리 타입", example = "BOARD", implementation = CategoryType.class, requiredMode = Schema.RequiredMode.REQUIRED)
    private CategoryType type;

    @Schema(description = "사이트 노출 여부", example = "true")
    private boolean siteExposed;

    @Min(0)
    @Schema(description = "사이트 노출 순서", example = "1")
    private Integer siteExposedOrder;

    @Schema(description = "노출 여부", example = "true")
    private boolean exposed;

    @NotNull
    @Min(0)
    @Schema(description = "정렬 순서", example = "10")
    private Integer sequence;

    @NotNull
    @NotBlank
    @Schema(description = "사이트 식별자(UUID)", example = "8f14e45f-ea9d-4b1c-a3a4-12c4b2a9c001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String siteId;

    @Schema(description = "부모 카테고리 식별자(UUID)", example = "8f14e45f-ea9d-4b1c-a3a4-12c4b2a9c001")
    private String parentId;

}
