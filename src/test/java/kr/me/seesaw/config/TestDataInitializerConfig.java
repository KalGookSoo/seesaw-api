package kr.me.seesaw.config;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import kr.me.seesaw.command.CreateArticleCommand;
import kr.me.seesaw.command.CreateCategoryCommand;
import kr.me.seesaw.command.CreateReplyCommand;
import kr.me.seesaw.domain.*;
import kr.me.seesaw.domain.Permission;
import kr.me.seesaw.domain.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.acls.domain.BasePermission;

import java.util.UUID;

/**
 * 테스트 전용 데이터 초기화 구성 클래스.
 * - test 프로필에서만 활성화됩니다.
 * - 필요 시 테스트 클래스에서 @Import(TestDataInitializerConfig.class)로 가져와 사용할 수 있습니다.
 */
@Profile("test")
@TestConfiguration
public class TestDataInitializerConfig {

    private static final Logger log = LoggerFactory.getLogger(TestDataInitializerConfig.class);

    @Bean
    public ApplicationRunner testDataInitializer(EntityManager entityManager) {
        return new ApplicationRunner() {
            @Override
            @Transactional
            public void run(ApplicationArguments args) {
                log.info("[테스트 데이터 시드] 시작");

                // 1) 역할(ROLE_ADMIN, ROLE_MANAGER, ROLE_USER)
                Role roleAdmin = getOrCreateRole(entityManager, RoleName.ROLE_ADMIN.name(), "최고관리자");
                Role roleManager = getOrCreateRole(entityManager, RoleName.ROLE_MANAGER.name(), "관리자");
                Role roleUser = getOrCreateRole(entityManager, RoleName.ROLE_USER.name(), "일반사용자");

                // 2) 사이트 2개
                Site site1 = getOrCreateSite(entityManager,
                        "테스트사이트1",
                        "test1.local",
                        "테스트용 사이트 #1",
                        "dist-a",
                        true,
                        true,
                        "tag1,tag2",
                        "010-1111-2222",
                        "인트로1",
                        "본문1"
                );
                Site site2 = getOrCreateSite(entityManager,
                        "테스트사이트2",
                        "test2.local",
                        "테스트용 사이트 #2",
                        "dist-b",
                        true,
                        true,
                        "tag3,tag4",
                        "010-3333-4444",
                        "인트로2",
                        "본문2"
                );

                // 3) 사용자 3명(admin/manager/user)
                User admin = getOrCreateUser(entityManager, "admin", "관리자", "admin@test.local");
                User manager = getOrCreateUser(entityManager, "manager", "매니저", "manager@test.local");
                User user = getOrCreateUser(entityManager, "user", "사용자", "user@test.local");

                // 사용자별 사이트 롤 매핑(간단히 site1 기준)
                mapRoleIfNeeded(entityManager, admin, roleAdmin, site1);
                mapRoleIfNeeded(entityManager, manager, roleManager, site1);
                mapRoleIfNeeded(entityManager, user, roleUser, site1);

                // 4) 카테고리(부모/자식)
                Category catNotice = getOrCreateCategory(entityManager, site1.getId(), "공지", "공지 카테고리", 0, 0);
                Category catFree = getOrCreateCategory(entityManager, site1.getId(), "자유", "자유 카테고리", 1, 1);
                // site2에도 하나 생성
                Category catIntro = getOrCreateCategory(entityManager, site2.getId(), "소개", "소개 카테고리", 0, 0);

                // 5) 권한(Permission) - targetId를 category.id에 맞춤
                createPermissionIfNeeded(entityManager, catNotice.getId(), roleAdmin.getId(), BasePermission.READ.getMask() + BasePermission.WRITE.getMask() + BasePermission.CREATE.getMask() + BasePermission.DELETE.getMask());
                createPermissionIfNeeded(entityManager, catNotice.getId(), roleManager.getId(), BasePermission.READ.getMask() + BasePermission.WRITE.getMask() + BasePermission.CREATE.getMask());
                createPermissionIfNeeded(entityManager, catFree.getId(), roleUser.getId(), BasePermission.READ.getMask() + BasePermission.WRITE.getMask());

                // 6) 게시글
                Article a1 = getOrCreateArticle(entityManager, catNotice.getId(), admin.getUsername(), "첫 번째 공지", "공지 본문입니다.");
                Article a2 = getOrCreateArticle(entityManager, catFree.getId(), manager.getUsername(), "첫 번째 자유글", "자유 본문입니다.");

                // 7) 댓글
                getOrCreateReply(entityManager, a1.getId(), user.getUsername(), "첫 번째 공지 댓글");
                getOrCreateReply(entityManager, a2.getId(), admin.getUsername(), "첫 번째 자유 댓글");

                // 8) 조회수(View)
                createViewIfNeeded(entityManager, a1.getId());
                createViewIfNeeded(entityManager, a2.getId());

                // 9) 투표(Vote)
                createVoteIfNeeded(entityManager, a1.getId(), true);
                createVoteIfNeeded(entityManager, a2.getId(), false);

                log.info("[테스트 데이터 시드] 완료");
            }
        };
    }

    private Role getOrCreateRole(EntityManager entityManager, String name, String alias) {
        return entityManager.createQuery("select r from Role r where r.name = :name", Role.class)
                .setParameter("name", name)
                .getResultList()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(name);
                    role.setAlias(alias);
                    entityManager.persist(role);
                    return role;
                });
    }

    private Site getOrCreateSite(EntityManager entityManager,
                                 String name,
                                 String domain,
                                 String description,
                                 String distributionCode,
                                 boolean searchEngineExposed,
                                 boolean imageExposed,
                                 String tags,
                                 String contactNumber,
                                 String intro,
                                 String content) {
        return entityManager.createQuery("select s from Site s where s.domainName = :domain", Site.class)
                .setParameter("domain", domain)
                .getResultList()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    Site site = new Site();
                    site.setName(name);
                    site.setDomainName(domain);
                    site.setDescription(description);
                    site.setDistributionCode(distributionCode);
                    site.setSearchEngineExposed(searchEngineExposed);
                    site.setImageExposed(imageExposed);
                    site.setTags(tags);
                    site.setContactNumber(contactNumber);
                    site.setIntro(intro);
                    site.setContent(content);
                    entityManager.persist(site);
                    return site;
                });
    }

    private User getOrCreateUser(EntityManager entityManager, String username, String name, String email) {
        return entityManager.createQuery("select u from User u where u.username = :username", User.class)
                .setParameter("username", username)
                .getResultList()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    String[] parts = email.split("@", 2);
                    Email em = new Email(parts[0], parts.length > 1 ? parts[1] : "test.local");
                    User u = new User();
                    u.setUsername(username);
                    u.setPassword("pass1234!");
                    u.setName(name);
                    u.setEmail(em);
                    u.setContactNumber("010-0000-0000");
                    u.initializeAccountPolicy();
                    entityManager.persist(u);
                    return u;
                });
    }

    private void mapRoleIfNeeded(EntityManager entityManager, User user, Role role, Site site) {
        boolean exists = user.getRoleMappings().stream()
                .anyMatch(rm -> role.equals(rm.getRole()) && site.equals(rm.getSite()));
        if (!exists) {
            RoleMapping rm = new RoleMapping();
            rm.setRole(role);
            rm.setUser(user);
            rm.setSite(site);
            user.addRole(rm); // cascade로 RoleMapping 저장
            entityManager.merge(user);
        }
    }

    private Category getOrCreateCategory(EntityManager entityManager, String siteId, String name, String desc, int siteExposedOrder, int sequence) {
        return entityManager.createQuery("select c from Category c where c.site.id = :siteId and c.name = :name", Category.class)
                .setParameter("siteId", siteId)
                .setParameter("name", name)
                .getResultList()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    Category category = new Category();
                    category.setName(name);
                    category.setDescription(desc);
                    category.setType(CategoryType.NONE);
                    category.setSiteExposed(true);
                    category.setSiteExposedOrder(siteExposedOrder);
                    category.setExposed(true);
                    category.setSequence(sequence);
                    
                    Site site = new Site();
                    site.setId(siteId);
                    category.setSite(site);

                    entityManager.persist(category);
                    return category;
                });
    }

    private void createPermissionIfNeeded(EntityManager entityManager, String targetId, String roleId, int mask) {
        Long count = entityManager.createQuery("select count(p) from Permission p where p.targetId = :targetId and p.roleId = :roleId", Long.class)
                .setParameter("targetId", targetId)
                .setParameter("roleId", roleId)
                .getSingleResult();

        if (count == 0) {
            Permission permission = new Permission();
            permission.setTargetId(targetId);
            permission.setRoleId(roleId);
            permission.setMask(mask);
            entityManager.persist(permission);
        }
    }

    private Article getOrCreateArticle(EntityManager entityManager, String categoryId, String createdBy, String title, String content) {
        return entityManager.createQuery("select a from Article a where a.title = :title", Article.class)
                .setParameter("title", title)
                .getResultList()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    Article article = new Article();
                    Category category = new Category();
                    category.setId(categoryId);
                    article.setCategory(category);
                    article.setType(ArticleType.HTML);
                    article.setFixed(false);
                    article.setFixedOrder(0);
                    article.setTitle(title);
                    article.setContent(content);

                    entityManager.persist(article);
                    return article;
                });
    }

    private void getOrCreateReply(EntityManager entityManager, String articleId, String createdBy, String content) {
        Long count = entityManager.createQuery("select count(r) from Reply r where r.content = :content", Long.class)
                .setParameter("content", content)
                .getSingleResult();

        if (count == 0) {
            Reply reply = new Reply();
            Article article = new Article();
            article.setId(articleId);
            reply.setArticle(article);
            reply.setContent(content);
            reply.setExposed(true);
            entityManager.persist(reply);
        }
    }

    private void createViewIfNeeded(EntityManager entityManager, String articleId) {
        Long count = entityManager.createQuery("select count(v) from View v where v.article.id = :articleId", Long.class)
                .setParameter("articleId", articleId)
                .getSingleResult();

        if (count == 0) {
            View view = new View();
            Article article = new Article();
            article.setId(articleId);
            view.setArticle(article);
            entityManager.persist(view);
        }
    }

    private void createVoteIfNeeded(EntityManager entityManager, String referenceId, boolean approved) {
        entityManager.createNativeQuery("INSERT INTO tb_vote (id, approved, reference_id, version) VALUES (?1, ?2, ?3, 0)")
                .setParameter(1, UUID.randomUUID().toString())
                .setParameter(2, approved)
                .setParameter(3, referenceId)
                .executeUpdate();
    }

}
