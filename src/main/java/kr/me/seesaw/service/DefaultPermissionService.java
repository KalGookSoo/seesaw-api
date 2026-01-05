package kr.me.seesaw.service;

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

}
