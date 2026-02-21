package kr.me.seesaw.service;

import kr.me.seesaw.domain.Role;
import kr.me.seesaw.model.RoleModel;
import kr.me.seesaw.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class DefaultRoleService implements RoleService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final RoleRepository roleRepository;

    @Override
    public List<RoleModel> getAllRoles() {
        logger.info("모든 권한 목록 조회");
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(RoleModel::new)
                .toList();
    }

}
