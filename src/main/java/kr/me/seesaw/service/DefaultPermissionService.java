package kr.me.seesaw.service;

import kr.me.seesaw.command.SavePermissionCommand;
import kr.me.seesaw.domain.Permission;
import kr.me.seesaw.model.PermissionModel;
import kr.me.seesaw.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class DefaultPermissionService implements PermissionService {

    private final PermissionRepository permissionRepository;

    @Override
    public List<PermissionModel> getPermissionsByTargetId(String targetId) {
        List<Permission> permissions = permissionRepository.findAllByTargetId(targetId);
        return permissions.stream()
                .map(PermissionModel::new)
                .toList();
    }

    @Override
    public PermissionModel saveCategoryPermission(SavePermissionCommand command) {
        Permission permission = permissionRepository.findByRoleIdAndTargetId(command.getRoleId(), command.getTargetId())
                .map(existingPermissions -> {
                    existingPermissions.update(command.getTargetId(), command.getRoleId(), command.getMask());
                    return existingPermissions;
                })
                .orElseGet(() -> Permission.create(command.getTargetId(), command.getRoleId(), command.getMask()));
        Permission savedPermission = permissionRepository.save(permission);
        return new PermissionModel(savedPermission);
    }

}
