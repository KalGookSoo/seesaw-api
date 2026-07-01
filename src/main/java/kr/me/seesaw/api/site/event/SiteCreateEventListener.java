package kr.me.seesaw.api.site.event;

import kr.me.seesaw.core.domain.role.Role;
import kr.me.seesaw.core.domain.mapping.RoleMapping;
import kr.me.seesaw.core.domain.site.Site;
import kr.me.seesaw.core.domain.user.User;
import kr.me.seesaw.core.domain.role.RoleName;
import kr.me.seesaw.api.site.SiteCreatedEvent;
import kr.me.seesaw.core.domain.role.RoleRepository;
import kr.me.seesaw.core.domain.site.SiteRepository;
import kr.me.seesaw.core.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Slf4j
@RequiredArgsConstructor
@Component
public class SiteCreateEventListener {

    private final SiteRepository siteRepository;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    @EventListener
    @Transactional(propagation = Propagation.REQUIRED)
    public void onSiteCreated(SiteCreatedEvent event) {
        log.debug("사이트 생성 이벤트 처리: {}", event);
        Site site = siteRepository.getReferenceById(event.siteId());
        User user = userRepository.findByUsername(event.username())
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. username: " + event.username()));

        Role managerRole = roleRepository.findByName(RoleName.ROLE_MANAGER.name())
                .orElseThrow(() -> new NoSuchElementException("관리자 역할이 없습니다."));

        RoleMapping roleMapping = new RoleMapping();
        roleMapping.setRole(managerRole);
        roleMapping.setUser(user);
        roleMapping.setSite(site);
        user.addRole(roleMapping);
        userRepository.save(user);
    }

}
