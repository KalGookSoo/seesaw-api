package kr.me.seesaw.api.permission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.me.seesaw.core.domain.permission.Permission;
import kr.me.seesaw.core.support.dto.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * 권한 표현 모델
 * - 직렬화 가능하고 불변 지향합니다.
 * - 엔터티 연관을 직접 접근하지 않습니다.
 */
@Schema(name = "PermissionResponse", description = "권한 모델")
@ToString
@EqualsAndHashCode(callSuper = true)
@Getter
@AllArgsConstructor
public final class PermissionResponse extends BaseResponse {

    @Schema(description = "대상 식별자", example = "대상 식별자")
    private final String targetId;

    @Schema(description = "역할 식별자", example = "역할 식별자")
    private final String roleId;

    @Schema(description = "비트마스크", example = "1")
    private final int mask;

    public PermissionResponse(Permission permission) {
        setBaseModel(permission);
        this.targetId = permission.getTargetId();
        this.roleId = permission.getRoleId();
        this.mask = permission.getMask();
    }

}
