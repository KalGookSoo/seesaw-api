package kr.me.seesaw.service;

import kr.me.seesaw.domain.Role;
import kr.me.seesaw.model.RoleModel;
import kr.me.seesaw.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class DefaultRoleService implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public List<RoleModel> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(RoleModel::new)
                .toList();
    }

}
