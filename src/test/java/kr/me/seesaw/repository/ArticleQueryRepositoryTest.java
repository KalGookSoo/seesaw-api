package kr.me.seesaw.repository;

import kr.me.seesaw.repository.impl.ArticleQueryRepositoryImpl;
import kr.me.seesaw.request.search.SearchArticlesRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

import java.time.LocalDateTime;

import static org.assertj.core.api.Fail.fail;

@ActiveProfiles({"test"})
@DataJpaTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class ArticleQueryRepositoryTest {

    private final TestEntityManager entityManager;

    private ArticleQueryRepository articleQueryRepository;

    public ArticleQueryRepositoryTest(TestEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @BeforeEach
    void setup() {
        articleQueryRepository = new ArticleQueryRepositoryImpl(entityManager.getEntityManager());
    }

    @Test
    @DisplayName("생성일시 기준 이전 하나 조회")
    void findFirst() {
        // Given
        SearchArticlesRequest search = new SearchArticlesRequest();
        search.setCategoryId("");
        search.setKeyWord("");
        search.setKeyField("");
        LocalDateTime createdDate = LocalDateTime.of(2020, 1, 1, 0, 0);

        // When & Then
        try {
            articleQueryRepository.findFirstNext(search, createdDate, "DESC");
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @DisplayName("생성일시 기준 다음 하나 조회")
    void findFirstAsc() {
        // Given
        SearchArticlesRequest search = new SearchArticlesRequest();
        search.setCategoryId("");
        search.setKeyWord("");
        search.setKeyField("");
        LocalDateTime createdDate = LocalDateTime.of(2020, 1, 1, 0, 0);

        // When & Then
        try {
            articleQueryRepository.findFirstNext(search, createdDate, "ASC");
        } catch (Exception e) {
            fail();
        }
    }

}
