package kr.me.seesaw.api.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.me.seesaw.core.domain.role.Role;
import kr.me.seesaw.core.support.dto.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * 역할 표현 모델
 * - 직렬화 가능하고 불변 지향합니다.
 * - 엔터티 연관을 직접 접근하지 않습니다.
 */
@Schema(name = "RoleResponse", description = "역할 모델")
@ToString
@EqualsAndHashCode(callSuper = true)
@Getter
@AllArgsConstructor
public final class RoleResponse extends BaseResponse {

    @Schema(description = "역할명", example = "ROLE_USER")
    private final String name;

    @Schema(description = "역할 별칭", example = "일반 사용자")
    private final String alias;

    public RoleResponse(Role role) {
        setBaseModel(role);
        this.name = role.getName();
        this.alias = role.getAlias();
    }

}
