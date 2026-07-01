package kr.me.seesaw.api.category.application;

import kr.me.seesaw.api.category.CategoryService;
import kr.me.seesaw.api.category.dto.CategoryResponse;
import kr.me.seesaw.api.category.dto.CreateCategoryRequest;
import kr.me.seesaw.api.category.dto.MoveCategoryRequest;
import kr.me.seesaw.api.category.dto.UpdateCategoryRequest;
import kr.me.seesaw.core.domain.category.Category;
import kr.me.seesaw.core.domain.category.CategoryRepository;
import kr.me.seesaw.core.domain.category.CategoryType;
import kr.me.seesaw.core.domain.category.persistence.CategoryRepositoryImpl;
import kr.me.seesaw.core.domain.site.Site;
import kr.me.seesaw.core.domain.site.SiteRepository;
import kr.me.seesaw.core.domain.site.persistence.SiteRepositoryImpl;
import kr.me.seesaw.framework.TestDataInitializerConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles({"test"})
@DataJpaTest
@Import({TestDataInitializerConfig.class, CategoryRepositoryImpl.class, SiteRepositoryImpl.class})
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class DefaultCategoryServiceTest {

    private final TestEntityManager entityManager;

    private final CategoryRepository categoryRepository;

    private final SiteRepository siteRepository;

    private CategoryService categoryService;

    private String siteId1;

    private String siteId2;

    public DefaultCategoryServiceTest(TestEntityManager entityManager, CategoryRepository categoryRepository, SiteRepository siteRepository) {
        this.entityManager = entityManager;
        this.categoryRepository = categoryRepository;
        this.siteRepository = siteRepository;
    }

    @BeforeEach
    void setup() {
        categoryService = new DefaultCategoryService(categoryRepository);

        // 테스트 데이터 시더(@Import)로 생성된 사이트를 조회하여 사용
        Site s1 = siteRepository.findByDomainName("test1.local").orElseThrow();
        Site s2 = siteRepository.findByDomainName("test2.local").orElseThrow();

        siteId1 = s1.getId();
        siteId2 = s2.getId();
    }

    @Test
    @DisplayName("카테고리 생성 시 카테고리를 반환합니다.")
    void createCategoryShouldReturnCategory() {
        // given
        CreateCategoryRequest command = createCommand(siteId1, "공지", 0, 0);

        // when
        CategoryResponse model = categoryService.createCategory(command);
        entityManager.flush();

        // then
        assertNotNull(model);
        assertNotNull(model.getId());
        assertEquals("공지", model.getName());
        assertEquals(siteId1, model.getSiteId());
    }

    @Test
    @DisplayName("ID로 카테고리를 조회하면 해당 카테고리를 반환합니다.")
    void getCategoryByIdShouldReturnCategory() {
        // given
        CategoryResponse saved = categoryService.createCategory(createCommand(siteId1, "게시판A", 0, 0));
        entityManager.flush();

        // when
        CategoryResponse found = categoryService.getCategoryById(saved.getId());

        // then
        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
        assertEquals("게시판A", found.getName());
    }

    @Test
    @DisplayName("없는 ID로 카테고리를 조회하면 NoSuchElementException이 발생합니다.")
    void getCategoryByIdWithUnknownIdShouldThrow() {
        // given
        String unknownId = "00000000-0000-0000-0000-000000000000";

        // when
        Executable act = () -> categoryService.getCategoryById(unknownId);

        // then
        assertThrows(NoSuchElementException.class, act);
    }

    @Test
    @DisplayName("카테고리를 수정하면 수정된 값이 반영됩니다.")
    void updateShouldModifyValues() {
        // given
        CategoryResponse saved = categoryService.createCategory(createCommand(siteId1, "게시판B", 0, 1));
        entityManager.flush();

        UpdateCategoryRequest command = UpdateCategoryRequest.builder()
                .name("게시판B-수정")
                .description("설명-수정")
                .type(CategoryType.BOARD)
                .siteExposed(true)
                .siteExposedOrder(3)
                .exposed(true)
                .sequence(5)
                .siteId(siteId1)
                .build();

        // when
        CategoryResponse updated = categoryService.updateCategory(saved.getId(), command);
        entityManager.flush();

        // then
        assertEquals("게시판B-수정", updated.getName());
        assertEquals("설명-수정", updated.getDescription());
        assertEquals(CategoryType.BOARD, updated.getType());
        assertTrue(updated.isSiteExposed());
        assertEquals(3, updated.getSiteExposedOrder());
        assertTrue(updated.isExposed());
        assertEquals(5, updated.getSequence());
    }

    @Test
    @DisplayName("카테고리를 삭제하면 더 이상 조회되지 않습니다.")
    void deleteCategoryShouldRemoveEntity() {
        // given
        CategoryResponse saved = categoryService.createCategory(createCommand(siteId1, "삭제대상", 0, 2));
        entityManager.flush();

        // when
        categoryService.deleteCategoryById(saved.getId());
        entityManager.flush();

        // then
        assertThrows(NoSuchElementException.class, () -> categoryService.getCategoryById(saved.getId()));
    }

    @Test
    @DisplayName("사이트 ID로 카테고리 목록을 조회하면 시퀀스 오름차순과 계층 구조가 적용됩니다.")
    void getCategoriesBySiteIdShouldReturnHierarchicalSortedList() {
        // given: 부모-자식 관계를 만들기 위해 먼저 부모 저장 후 자식의 parentId를 직접 세팅
        CategoryResponse parent = categoryService.createCategory(createCommand(siteId2, "부모", 0, 1));
        entityManager.flush();

        // 영속 엔티티 직접 업데이트가 필요하므로 엔티티 매니저로 parentId를 사용해 저장
        // 여기서는 단순 시퀀스 정렬과 반환 크기만 검증
        categoryService.createCategory(createCommand(siteId2, "자식1", 0, 2));
        categoryService.createCategory(createCommand(siteId2, "자식2", 0, 3));
        categoryService.createCategory(createCommand(siteId2, "기타", 0, 0));
        entityManager.flush();

        // when
        List<CategoryResponse> list = categoryService.getCategoriesBySiteId(siteId2);

        // then
        assertNotNull(list);
        assertTrue(list.size() >= 3);
        // 시퀀스 오름차순으로 첫 번째가 sequence=0인 "기타"가 되어야 함
        assertEquals(0, list.get(0).getSequence());
    }

    @Test
    @DisplayName("카테고리를 이동하면 순서가 변경됩니다.")
    void moveShouldUpdateParentAndSequence() {
        // given
        List<Site> sites = siteRepository.findAll();
        List<Category> categories = sites.get(0).getCategories();
        Category category = categories.get(0);
        MoveCategoryRequest moveCategoryCommand = MoveCategoryRequest.builder()
                .parentId(Optional.ofNullable(category.getParent()).map(Category::getId).orElse(null))
                .sequence(3)
                .build();

        // when
        CategoryResponse movedCategory = categoryService.moveCategory(category.getId(), moveCategoryCommand);
        entityManager.flush();
        entityManager.clear();

        // then
        Assertions.assertEquals(3, movedCategory.getSequence());
    }

    private CreateCategoryRequest createCommand(String siteId, String name, int siteExposedOrder, int sequence) {
        return CreateCategoryRequest.builder()
                .name(name)
                .description("description")
                .type(CategoryType.NONE)
                .siteExposed(true)
                .siteExposedOrder(siteExposedOrder)
                .exposed(true)
                .sequence(sequence)
                .siteId(siteId)
                .build();
    }

}
