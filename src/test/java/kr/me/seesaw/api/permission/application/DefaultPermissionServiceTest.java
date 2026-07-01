package kr.me.seesaw.api.permission.application;

import jakarta.persistence.EntityManager;
import kr.me.seesaw.api.permission.dto.PermissionResponse;
import kr.me.seesaw.api.permission.dto.SavePermissionRequest;
import kr.me.seesaw.core.domain.category.Category;
import kr.me.seesaw.core.domain.role.Role;
import kr.me.seesaw.framework.TestDataInitializerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("권한 서비스 테스트")
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Import(TestDataInitializerConfig.class)
@SpringBootTest
@Transactional
class DefaultPermissionServiceTest {

    private final DefaultPermissionService permissionService;

    private final EntityManager entityManager;

    public DefaultPermissionServiceTest(DefaultPermissionService permissionService, EntityManager entityManager) {
        this.permissionService = permissionService;
        this.entityManager = entityManager;
    }

    @Test
    @DisplayName("대상 식별자로 권한 목록을 조회합니다.")
    void getPermissionsByTargetId() {
        // given
        Category category = entityManager.createQuery("SELECT c FROM Category c WHERE c.name = :name", Category.class)
                .setParameter("name", "공지")
                .getSingleResult();

        // when
        List<PermissionResponse> permissions = permissionService.getPermissionsByTargetId(category.getId());

        // then
        assertThat(permissions).isNotEmpty();
        permissions.forEach(p -> assertThat(p.getTargetId()).isEqualTo(category.getId()));
    }

    @Test
    @DisplayName("카테고리 권한을 저장합니다. (신규 생성)")
    void saveCategoryPermission_Create() {
        // given
        Category category = entityManager.createQuery("SELECT c FROM Category c WHERE c.name = :name", Category.class)
                .setParameter("name", "자유")
                .getSingleResult();
        Role role = entityManager.createQuery("SELECT r FROM Role r WHERE r.name = :name", Role.class)
                .setParameter("name", "ROLE_ADMIN")
                .getSingleResult();

        // 자유 카테고리에 ROLE_ADMIN 권한은 초기 데이터에 없으므로 신규 생성 케이스

        SavePermissionRequest command = new SavePermissionRequest();
        command.setTargetId(category.getId());
        command.setRoleId(role.getId());
        command.setMask(7); // Read, Write, Create

        // when
        PermissionResponse result = permissionService.savePermission(command);

        // then
        assertThat(result.getTargetId()).isEqualTo(category.getId());
        assertThat(result.getRoleId()).isEqualTo(role.getId());
        assertThat(result.getMask()).isEqualTo(7);
    }

    @Test
    @DisplayName("카테고리 권한을 저장합니다. (기존 수정)")
    void saveCategoryPermission_Update() {
        // given
        Category category = entityManager.createQuery("SELECT c FROM Category c WHERE c.name = :name", Category.class)
                .setParameter("name", "공지")
                .getSingleResult();
        Role role = entityManager.createQuery("SELECT r FROM Role r WHERE r.name = :name", Role.class)
                .setParameter("name", "ROLE_ADMIN")
                .getSingleResult();

        SavePermissionRequest command = new SavePermissionRequest();
        command.setTargetId(category.getId());
        command.setRoleId(role.getId());
        command.setMask(1); // 기존 15(R+W+C+D)에서 1(R)로 수정

        // when
        PermissionResponse result = permissionService.savePermission(command);

        // then
        assertThat(result.getTargetId()).isEqualTo(category.getId());
        assertThat(result.getRoleId()).isEqualTo(role.getId());
        assertThat(result.getMask()).isEqualTo(1);

        // 실제 DB(영속성 컨텍스트)에 반영되었는지 재확인
        List<PermissionResponse> permissions = permissionService.getPermissionsByTargetId(category.getId());
        assertThat(permissions).filteredOn(p -> p.getRoleId().equals(role.getId()))
                .extracting(PermissionResponse::getMask)
                .containsExactly(1);
    }

}
