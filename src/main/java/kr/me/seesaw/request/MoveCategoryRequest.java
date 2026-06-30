package kr.me.seesaw.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Schema(name = "MoveCategoryRequest", description = "카테고리 이동 커맨드")
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoveCategoryRequest {

    @Schema(description = "부모 카테고리 식별자", example = "8f14e45f-ea9d-4b1c-a3a4-12c4b2a9c001")
    private String parentId;

    @NotNull
    @Min(0)
    @Schema(description = "정렬 순서", example = "10")
    private Integer sequence;

}
