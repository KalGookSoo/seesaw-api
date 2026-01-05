package kr.me.seesaw.config;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import kr.me.seesaw.command.CreateArticleCommand;
import kr.me.seesaw.command.CreateCategoryCommand;
import kr.me.seesaw.command.CreateReplyCommand;
import kr.me.seesaw.domain.*;
import kr.me.seesaw.domain.vo.ArticleType;
import kr.me.seesaw.domain.vo.CategoryType;
import kr.me.seesaw.domain.vo.Email;
import kr.me.seesaw.domain.vo.RoleName;
import kr.me.seesaw.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.util.Optional;
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
    public ApplicationRunner testDataInitializer(
            EntityManager entityManager,
            UserRepository userRepository,
            RoleRepository roleRepository,
            SiteRepository siteRepository,
            CategoryRepository categoryRepository,
            ArticleRepository articleRepository,
            ReplyRepository replyRepository,
            ViewRepository viewRepository
    ) {
        return new ApplicationRunner() {
            @Override
            @Transactional
            public void run(ApplicationArguments args) {
                log.info("[테스트 데이터 시드] 시작");

                // 1) 역할(ROLE_ADMIN, ROLE_MANAGER, ROLE_USER)
                Role roleAdmin = getOrCreateRole(roleRepository, RoleName.ROLE_ADMIN.name(), "최고관리자");
                Role roleManager = getOrCreateRole(roleRepository, RoleName.ROLE_MANAGER.name(), "관리자");
                Role roleUser = getOrCreateRole(roleRepository, RoleName.ROLE_USER.name(), "일반사용자");

                // 2) 사이트 2개
                Site site1 = getOrCreateSite(siteRepository,
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
                Site site2 = getOrCreateSite(siteRepository,
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
                User admin = getOrCreateUser(userRepository, "admin", "관리자", "admin@test.local");
                User manager = getOrCreateUser(userRepository, "manager", "매니저", "manager@test.local");
                User user = getOrCreateUser(userRepository, "user", "사용자", "user@test.local");

                // 사용자별 사이트 롤 매핑(간단히 site1 기준)
                mapRoleIfNeeded(userRepository, admin, roleAdmin, site1);
                mapRoleIfNeeded(userRepository, manager, roleManager, site1);
                mapRoleIfNeeded(userRepository, user, roleUser, site1);

                // 4) 카테고리(부모/자식)
                Category catNotice = getOrCreateCategory(categoryRepository, site1.getId(), "공지", "공지 카테고리", 0, 0);
                Category catFree = getOrCreateCategory(categoryRepository, site1.getId(), "자유", "자유 카테고리", 1, 1);
                // site2에도 하나 생성
                Category getOrCreateCategory = getOrCreateCategory(categoryRepository, site2.getId(), "소개", "소개 카테고리", 0, 0);

                // 5) 게시글
                Article a1 = getOrCreateArticle(articleRepository, catNotice.getId(), admin.getUsername(), "첫 번째 공지", "공지 본문입니다.");
                Article a2 = getOrCreateArticle(articleRepository, catFree.getId(), manager.getUsername(), "첫 번째 자유글", "자유 본문입니다.");

                // 6) 댓글
                getOrCreateReply(replyRepository, a1.getId(), user.getUsername(), "첫 번째 공지 댓글");
                getOrCreateReply(replyRepository, a2.getId(), admin.getUsername(), "첫 번째 자유 댓글");

                // 7) 조회수(View)
                createViewIfNeeded(viewRepository, a1.getId());
                createViewIfNeeded(viewRepository, a2.getId());

                // 8) 투표(Vote) - VoteRepository가 없어 EntityManager로 단순 삽입
                createVoteIfNeeded(entityManager, a1.getId(), true);
                createVoteIfNeeded(entityManager, a2.getId(), false);

                log.info("[테스트 데이터 시드] 완료");
            }
        };
    }

    private Role getOrCreateRole(RoleRepository roleRepository, String name, String alias) {
        Optional<Role> found = roleRepository.findByName(name);
        if (found.isPresent()) {
            return found.get();
        }
        Role role = Role.create(name, alias);
        return roleRepository.save(role);
    }

    private Site getOrCreateSite(SiteRepository siteRepository,
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
        return siteRepository.findByDomainName(domain).orElseGet(() -> {
            Site site = Site.create(name, domain, description, distributionCode, searchEngineExposed, imageExposed,
                    tags, null, contactNumber, intro, content);
            return siteRepository.save(site);
        });
    }

    private User getOrCreateUser(UserRepository userRepository, String username, String name, String email) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            String[] parts = email.split("@", 2);
            Email em = new Email(parts[0], parts.length > 1 ? parts[1] : "test.local");
            User u = User.create(username, "pass1234!", name, em, "010-0000-0000");
            return userRepository.save(u);
        });
    }

    private void mapRoleIfNeeded(UserRepository userRepository, User user, Role role, Site site) {
        boolean exists = user.getRoleMappings().stream()
                .anyMatch(rm -> role.equals(rm.getRole()) && site.equals(rm.getSite()));
        if (!exists) {
            RoleMapping rm = RoleMapping.create(role, user, site);
            user.addRole(rm); // cascade로 RoleMapping 저장
            userRepository.save(user);
        }
    }

    private Category getOrCreateCategory(CategoryRepository categoryRepository, String siteId, String name, String desc, int siteExposedOrder, int sequence) {
        CreateCategoryCommand cmd = CreateCategoryCommand.builder()
                .name(name)
                .description(desc)
                .type(CategoryType.NONE)
                .siteExposed(true)
                .siteExposedOrder(siteExposedOrder)
                .exposed(true)
                .sequence(sequence)
                .siteId(siteId)
                .build();
        Category category = Category.create(cmd);
        return categoryRepository.save(category);
    }

    private Article getOrCreateArticle(ArticleRepository articleRepository, String categoryId, String createdBy, String title, String content) {
        CreateArticleCommand cmd = new CreateArticleCommand();
        cmd.setCategoryId(categoryId);
        cmd.setType(ArticleType.HTML);
        cmd.setFixed(false);
        cmd.setFixedOrder(0);
        cmd.setTitle(title);
        cmd.setContent(content);
        Article article = Article.create(cmd);
        articleRepository.save(article);
        return article;
    }

    private void getOrCreateReply(ReplyRepository replyRepository, String articleId, String createdBy, String content) {
        CreateReplyCommand cmd = new CreateReplyCommand();
        cmd.setArticleId(articleId);
        cmd.setContent(content);
        cmd.setExposed(true);
        Reply reply = Reply.create(cmd);
        replyRepository.save(reply);
    }

    private void createViewIfNeeded(ViewRepository viewRepository, String articleId) {
        View view = View.create(articleId);
        viewRepository.save(view);
    }

    private void createVoteIfNeeded(EntityManager entityManager, String referenceId, boolean approved) {
        entityManager.createNativeQuery("INSERT INTO tb_vote (id, approved, reference_id, version) VALUES (?1, ?2, ?3, 0)")
                .setParameter(1, UUID.randomUUID().toString())
                .setParameter(2, approved)
                .setParameter(3, referenceId)
                .executeUpdate();
    }

}
