package kr.me.seesaw.service;

import kr.me.seesaw.command.SavePermissionCommand;
import kr.me.seesaw.domain.Permission;
import kr.me.seesaw.model.PermissionModel;
import kr.me.seesaw.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class DefaultPermissionService implements PermissionService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final PermissionRepository permissionRepository;

    @Override
    public List<PermissionModel> getPermissionsByTargetId(String targetId) {
        logger.debug("대상별 권한 목록 조회: targetId={}", targetId);
        List<Permission> permissions = permissionRepository.findAllByTargetId(targetId);
        return permissions.stream()
                .map(PermissionModel::new)
                .toList();
    }

    @Override
    public PermissionModel savePermission(SavePermissionCommand command) {
        logger.info("권한 저장: command={}", command);
        Permission permission = permissionRepository.findByRoleIdAndTargetId(command.getRoleId(), command.getTargetId())
                .map(existingPermissions -> {
                    existingPermissions.setTargetId(command.getTargetId());
                    existingPermissions.setRoleId(command.getRoleId());
                    existingPermissions.setMask(command.getMask());
                    return existingPermissions;
                })
                .orElseGet(() -> {
                    Permission newPermission = new Permission();
                    newPermission.setTargetId(command.getTargetId());
                    newPermission.setRoleId(command.getRoleId());
                    newPermission.setMask(command.getMask());
                    return newPermission;
                });
        Permission savedPermission = permissionRepository.save(permission);
        return new PermissionModel(savedPermission);
    }

}
