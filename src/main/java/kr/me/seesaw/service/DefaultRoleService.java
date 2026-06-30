package kr.me.seesaw.service;

import kr.me.seesaw.domain.Permission;
import kr.me.seesaw.domain.Role;
import kr.me.seesaw.response.PermissionResponse;
import kr.me.seesaw.response.RoleResponse;
import kr.me.seesaw.repository.PermissionRepository;
import kr.me.seesaw.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Transactional
@RequiredArgsConstructor
@Service
public class DefaultRoleService implements RoleService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final RoleRepository roleRepository;

    private final PermissionRepository permissionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        logger.info("모든 권한 목록 조회");
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(RoleResponse::new)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRole(String name) {
        logger.debug("권한 조회: name={}", name);
        Role role = roleRepository.findByName(name)
                .orElseThrow(NoSuchElementException::new);
        return new RoleResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getPermissions(String roleId) {
        logger.debug("권한 목록 조회: roleId={}", roleId);
        return permissionRepository.findAllByRoleId(roleId)
                .stream()
                .map(PermissionResponse::new)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResponse getPermission(String roleId, String targetId) {
        logger.debug("상세 권한 조회: roleId={}, targetId={}", roleId, targetId);
        Permission permission = permissionRepository.findByRoleIdAndTargetId(roleId, targetId)
                .orElseThrow(NoSuchElementException::new);
        return new PermissionResponse(permission);
    }

}
