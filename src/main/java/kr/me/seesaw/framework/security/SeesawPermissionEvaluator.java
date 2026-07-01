package kr.me.seesaw.framework.security;

import kr.me.seesaw.api.role.RoleService;
import kr.me.seesaw.api.permission.dto.PermissionResponse;
import kr.me.seesaw.api.role.dto.RoleResponse;
import kr.me.seesaw.core.domain.role.RoleName;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class SeesawPermissionEvaluator implements PermissionEvaluator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final RoleService roleService;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || permission == null) {
            logger.warn("인증된 계정 정보를 찾을 수 없습니다. Authentication: {}", authentication);
            return false;
        }

        if (!(permission instanceof BasePermission basePermission)) {
            logger.error("hasPermission을 사용할 때 BasePermission 하위타입을 사용하세요. 현재 Permission Type: {}", permission.getClass().getName());
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities == null || authorities.isEmpty()) {
            logger.warn("계정에 종속된 권한이 없습니다.");
            return false;
        }

        Set<String> authoritySet = AuthorityUtils.authorityListToSet(authorities);

        if (authoritySet.contains(RoleName.ROLE_ANONYMOUS.name())) {
            return false;
        }

        // 사용자의 각 역할에 대해 권한 확인
        for (GrantedAuthority authority : authorities) {
            String roleName = authority.getAuthority();

            try {
                // 역할 이름으로 역할 조회
                RoleResponse role = roleService.getRole(roleName);

                // 역할 ID로 권한 조회
                List<PermissionResponse> permissions = roleService.getPermissions(role.getId());

                if (permissions.isEmpty()) {
                    logger.info("역할 {}에 대한 권한이 없습니다.", roleName);
                    continue;
                }

                // 각 권한에 대해 확인
                for (PermissionResponse permissionByRoleId : permissions) {
                    // 권한 마스크 비교
                    int permissionMask = permissionByRoleId.getMask();
                    int requiredMask = basePermission.getMask();

                    // 비트 연산으로 권한 확인 (requiredMask가 permissionMask에 포함되어 있는지)
                    if ((permissionMask & requiredMask) == requiredMask) {
                        return true;
                    }
                }
            } catch (RuntimeException e) {
                logger.warn("역할 {}에 대한 권한 확인 중 오류 발생", roleName);
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || targetId == null || targetType == null || permission == null) {
            logger.warn("인증 정보 또는 대상 정보가 없습니다. Authentication: {}, targetId: {}, targetType: {}", authentication,
                    targetId, targetType);
            return false;
        }

        if (!(permission instanceof BasePermission basePermission)) {
            logger.error("hasPermission을 사용할 때 BasePermission 하위타입을 사용하세요. 현재 Permission Type: {}", permission.getClass().getName());
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities == null || authorities.isEmpty()) {
            logger.warn("계정에 종속된 권한이 없습니다.");
            return false;
        }

        Set<String> authoritySet = AuthorityUtils.authorityListToSet(authorities);

        if (authoritySet.contains(RoleName.ROLE_ANONYMOUS.name())) {
            return false;
        }

        // 사용자의 각 역할에 대해 권한 확인
        for (GrantedAuthority authority : authorities) {
            String roleName = authority.getAuthority();

            try {
                // 역할 이름으로 역할 조회
                RoleResponse role = roleService.getRole(roleName);

                // 역할 ID와 대상 ID로 권한 조회
                PermissionResponse permissionByRoleId = roleService.getPermission(role.getId(), targetId.toString());

                // 권한 마스크 비교
                int permissionMask = permissionByRoleId.getMask();
                int requiredMask = basePermission.getMask();

                // 비트 연산으로 권한 확인 (requiredMask가 permissionMask에 포함되어 있는지)
                if ((permissionMask & requiredMask) == requiredMask) {
                    return true;
                }
            } catch (RuntimeException e) {
                logger.warn("역할 {}에 대한 권한 확인 중 오류 발생", roleName);
                return false;
            }
        }
        return false;
    }

}
