package kr.me.seesaw.api.role;

import kr.me.seesaw.api.permission.dto.PermissionResponse;
import kr.me.seesaw.api.role.dto.RoleResponse;

import java.util.List;

public interface RoleService {

    List<RoleResponse> getAllRoles();

    RoleResponse getRole(String name);

    List<PermissionResponse> getPermissions(String roleId);

    PermissionResponse getPermission(String roleId, String targetId);

}
