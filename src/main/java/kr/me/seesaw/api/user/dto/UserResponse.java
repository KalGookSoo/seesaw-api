package kr.me.seesaw.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.me.seesaw.api.role.dto.RoleResponse;
import kr.me.seesaw.core.domain.user.User;
import kr.me.seesaw.core.support.dto.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Schema(name = "UserResponse", description = "계정 모델")
@ToString(exclude = {"password", "roles"})
@EqualsAndHashCode(callSuper = true)
@Getter
@AllArgsConstructor
public final class UserResponse extends BaseResponse {

    @Schema(description = "계정명")
    private final String username;

    @Schema(description = "이름")
    private final String name;

    @Schema(description = "이메일", example = "user@example.com")
    private final String email;

    @Schema(description = "연락처")
    private final String contactNumber;

    @Schema(description = "만료 일시")
    private final LocalDateTime expiredDate;

    @Schema(description = "잠금 일시")
    private final LocalDateTime lockedDate;

    @Schema(description = "패스워드 만료 일시")
    private final LocalDateTime credentialsExpiredDate;

    @Schema(description = "역할 목록", accessMode = Schema.AccessMode.READ_ONLY)
    private final Set<RoleResponse> roles = new LinkedHashSet<>();

    @Schema(description = "패스워드", accessMode = Schema.AccessMode.READ_ONLY)
    private final String password;

    public UserResponse(User user) {
        setBaseModel(user);
        this.username = user.getUsername();
        this.name = user.getName();
        this.email = user.getEmail().toString();
        this.contactNumber = user.getContactNumber();
        this.expiredDate = user.getExpiredDate();
        this.lockedDate = user.getLockedDate();
        this.credentialsExpiredDate = user.getCredentialsExpiredDate();
        this.password = user.getPassword();
    }

    /**
     * 계정이 만료되지 않았는지 여부를 반환합니다.
     */
    public boolean isAccountNonExpired() {
        return expiredDate == null || expiredDate.isAfter(LocalDateTime.now());
    }

    /**
     * 계정이 잠겨있지 않은지 여부를 반환합니다.
     */
    public boolean isAccountNonLocked() {
        return lockedDate == null || lockedDate.isBefore(LocalDateTime.now());
    }

    /**
     * 패스워드가 만료되지 않았는지 여부를 반환합니다.
     */
    public boolean isCredentialsNonExpired() {
        return credentialsExpiredDate == null || credentialsExpiredDate.isAfter(LocalDateTime.now());
    }

    public void addRole(RoleResponse role) {
        roles.add(role);
    }

}
