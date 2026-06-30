package kr.me.seesaw.service;

import kr.me.seesaw.response.PermissionResponse;
import kr.me.seesaw.response.RoleResponse;

import java.util.List;

public interface RoleService {

    List<RoleResponse> getAllRoles();

    RoleResponse getRole(String name);

    List<PermissionResponse> getPermissions(String roleId);

    PermissionResponse getPermission(String roleId, String targetId);

}
