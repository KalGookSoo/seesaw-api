package kr.me.seesaw.api.article.presentation;

import kr.me.seesaw.api.article.ArticleContext;
import kr.me.seesaw.api.article.ArticleService;
import kr.me.seesaw.api.article.dto.ArticleDetailResponse;
import kr.me.seesaw.api.article.dto.ArticleResponse;
import kr.me.seesaw.api.article.dto.SearchArticleDetailRequest;
import kr.me.seesaw.api.article.dto.SearchArticlesRequest;
import kr.me.seesaw.api.category.CategoryNotExposedException;
import kr.me.seesaw.api.category.CategoryService;
import kr.me.seesaw.api.category.dto.CategoryResponse;
import kr.me.seesaw.core.domain.article.Article;
import kr.me.seesaw.core.domain.category.CategoryType;
import kr.me.seesaw.core.support.message.CmsMessageSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleApiControllerTest {

    @Mock
    private CmsMessageSource messageSource;

    @Mock
    private ArticleContext articleContext;

    @Mock
    private ArticleService articleService;

    @Mock
    private CategoryService categoryService;

    private ArticleApiController articleApiController;

    @BeforeEach
    void setUp() {
        articleApiController = new ArticleApiController(
                messageSource,
                articleContext,
                articleService,
                categoryService
        );
    }

    @Test
    void 상세_게시글의_카테고리로_검색조건을_보완하고_비노출_인접글은_제외한다() {
        String categoryId = UUID.randomUUID().toString();
        String hiddenCategoryId = UUID.randomUUID().toString();
        String exposedCategoryId = UUID.randomUUID().toString();
        LocalDateTime createdDate = LocalDateTime.now();
        ArticleResponse article = articleResponse(UUID.randomUUID().toString(), categoryId, createdDate);
        ArticleResponse previousArticle = articleResponse(UUID.randomUUID().toString(), hiddenCategoryId, createdDate.minusDays(1));
        ArticleResponse nextArticle = articleResponse(UUID.randomUUID().toString(), exposedCategoryId, createdDate.plusDays(1));
        CategoryResponse category = org.mockito.Mockito.mock(CategoryResponse.class);
        CategoryResponse hiddenCategory = org.mockito.Mockito.mock(CategoryResponse.class);
        CategoryResponse exposedCategory = org.mockito.Mockito.mock(CategoryResponse.class);

        when(articleContext.find(article.getId())).thenReturn(article);
        when(articleContext.getArticleAggregation(article.getId())).thenReturn(article);
        when(categoryService.getCategoryById(categoryId)).thenReturn(category);
        when(categoryService.getCategoryById(hiddenCategoryId)).thenReturn(hiddenCategory);
        when(categoryService.getCategoryById(exposedCategoryId)).thenReturn(exposedCategory);
        when(category.isExposed()).thenReturn(true);
        when(category.getType()).thenReturn(CategoryType.BOARD);
        when(hiddenCategory.isExposed()).thenReturn(false);
        when(exposedCategory.isExposed()).thenReturn(true);
        when(articleContext.getPreviousArticle(any(SearchArticlesRequest.class), eq(createdDate))).thenReturn(previousArticle);
        when(articleContext.getNextArticle(any(SearchArticlesRequest.class), eq(createdDate))).thenReturn(nextArticle);

        ResponseEntity<ArticleDetailResponse> result = articleApiController.getArticle(
                article.getId(),
                SearchArticleDetailRequest.builder().build()
        );

        ArticleDetailResponse response = result.getBody();
        assertEquals(article, response.article());
        assertNull(response.previousArticleId());
        assertEquals(nextArticle.getId(), response.nextArticleId());

        ArgumentCaptor<SearchArticlesRequest> searchCaptor = ArgumentCaptor.forClass(SearchArticlesRequest.class);
        verify(articleContext).getPreviousArticle(searchCaptor.capture(), eq(createdDate));
        assertEquals(categoryId, searchCaptor.getValue().getCategoryId());
        assertEquals(CategoryType.BOARD, searchCaptor.getValue().getCategoryType());
    }

    @Test
    void 비노출_카테고리의_게시글은_전용_예외를_발생시킨다() {
        String categoryId = UUID.randomUUID().toString();
        ArticleResponse article = articleResponse(UUID.randomUUID().toString(), categoryId, LocalDateTime.now());
        CategoryResponse category = org.mockito.Mockito.mock(CategoryResponse.class);

        when(articleContext.find(article.getId())).thenReturn(article);
        when(categoryService.getCategoryById(categoryId)).thenReturn(category);
        when(category.isExposed()).thenReturn(false);

        assertThrows(
                CategoryNotExposedException.class,
                () -> articleApiController.getArticle(
                        article.getId(),
                        SearchArticleDetailRequest.builder().build()
                )
        );

        verify(articleContext, never()).getArticleAggregation(article.getId());
    }

    private ArticleResponse articleResponse(String id, String categoryId, LocalDateTime createdDate) {
        Article article = new Article();
        article.setId(id);
        article.setCategoryId(categoryId);
        article.setCreatedDate(createdDate);
        return new ArticleResponse(article);
    }

}
