package kr.me.seesaw.repository;

import kr.me.seesaw.search.ArticleSearch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Fail.fail;

@ActiveProfiles({"test"})
@DataJpaTest
class ArticleSearchRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    private ArticleSearchRepository articleSearchRepository;

    @BeforeEach
    void setup() {
        articleSearchRepository = new ArticleSearchRepository(entityManager.getEntityManager());
    }

    @Test
    @DisplayName("생성일시 기준 이전 하나 조회")
    void findFirst() {
        // Given
        ArticleSearch search = new ArticleSearch();
        search.setCategoryId("");
        search.setKeyWord("");
        search.setKeyField("");
        Sort sort = Sort.by(Sort.Direction.DESC, "createdDate");
        LocalDateTime createdDate = LocalDateTime.of(2020, 1, 1, 0, 0);

        // When & Then
        try {
            articleSearchRepository.findFirstNext(search, createdDate, sort);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @DisplayName("생성일시 기준 다음 하나 조회")
    void findFirstAsc() {
        // Given
        ArticleSearch search = new ArticleSearch();
        search.setCategoryId("");
        search.setKeyWord("");
        search.setKeyField("");
        Sort sort = Sort.by(Sort.Direction.ASC, "createdDate");
        LocalDateTime createdDate = LocalDateTime.of(2020, 1, 1, 0, 0);

        // When & Then
        try {
            articleSearchRepository.findFirstNext(search, createdDate, sort);
        } catch (Exception e) {
            fail();
        }
    }

}