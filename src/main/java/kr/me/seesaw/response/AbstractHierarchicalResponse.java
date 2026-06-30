package kr.me.seesaw.response;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@ToString(exclude = {"parent", "children"})
@EqualsAndHashCode(exclude = {"parent", "children"}, callSuper = true)
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor
abstract public class AbstractHierarchicalResponse<T> extends BaseResponse {

    @Schema(description = "부모 식별자", example = "c1e2d3f4-0000-0000-0000-000000000001")
    private String parentId;

    @JsonBackReference
    @Schema(description = "부모 모델 참조", accessMode = Schema.AccessMode.READ_ONLY)
    private T parent;

    @JsonManagedReference
    @Schema(description = "자식 모델 목록", accessMode = Schema.AccessMode.READ_ONLY)
    private List<T> children = new ArrayList<>();

}
