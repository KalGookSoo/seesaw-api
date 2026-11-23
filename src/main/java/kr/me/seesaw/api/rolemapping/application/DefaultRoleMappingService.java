package kr.me.seesaw.api.rolemapping.application;

import kr.me.seesaw.api.rolemapping.RoleMappingAdminRemovalNotAllowedException;
import kr.me.seesaw.api.rolemapping.RoleMappingService;
import kr.me.seesaw.core.domain.mapping.RoleMapping;
import kr.me.seesaw.core.domain.mapping.RoleMappingRepository;
import kr.me.seesaw.core.domain.role.RoleName;
import kr.me.seesaw.core.domain.user.User;
import kr.me.seesaw.core.domain.user.UserRepository;
import kr.me.seesaw.core.support.authentication.PrincipalProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Collection;

@Transactional
@Service
@RequiredArgsConstructor
public class DefaultRoleMappingService implements RoleMappingService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final RoleMappingRepository roleMappingRepository;

    private final UserRepository userRepository;

    private final PrincipalProvider principalProvider;

    @Override
    public void removeMyRoleMapping(String siteId) {
        String username = principalProvider.getAuthentication().getName();
        logger.info("역할 매핑 제거: username={}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("해당 계정이 존재하지 않습니다."));
        Collection<RoleMapping> roleMappings = roleMappingRepository.findAllByUserIdAndSiteId(user.getId(), siteId);
        if (roleMappings.isEmpty()) {
            throw new NoSuchElementException("제거할 역할 매핑이 존재하지 않습니다.");
        }

        if (roleMappings.stream().anyMatch(roleMapping -> roleMapping.getRole() != null
                && roleMapping.getRole().has(RoleName.ROLE_ADMIN))) {
            throw new RoleMappingAdminRemovalNotAllowedException(siteId);
        }

        roleMappingRepository.deleteByUserIdAndSiteId(user.getId(), siteId);
    }

}
