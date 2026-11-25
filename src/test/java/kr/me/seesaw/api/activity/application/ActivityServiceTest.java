package kr.me.seesaw.api.activity.application;

import java.util.List;
import kr.me.seesaw.api.category.CategoryPermissionEvaluator;
import kr.me.seesaw.core.domain.article.Article;
import kr.me.seesaw.core.domain.article.ArticleRepository;
import kr.me.seesaw.core.domain.category.Category;
import kr.me.seesaw.core.domain.category.CategoryRepository;
import kr.me.seesaw.core.domain.reply.ReplyRepository;
import kr.me.seesaw.core.domain.vote.VoteRepository;
import kr.me.seesaw.core.support.authentication.PrincipalProvider;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ActivityServiceTest {
    @Test
    void 읽기_권한이_있는_카테고리의_본인_게시글만_조회한다() {
        ArticleRepository articles = mock(ArticleRepository.class);
        ReplyRepository replies = mock(ReplyRepository.class);
        VoteRepository votes = mock(VoteRepository.class);
        CategoryRepository categories = mock(CategoryRepository.class);
        CategoryPermissionEvaluator permissions = mock(CategoryPermissionEvaluator.class);
        PrincipalProvider principal = mock(PrincipalProvider.class);
        Category allowed = new Category(); allowed.setId("allowed"); allowed.setName("게시판");
        Category denied = new Category(); denied.setId("denied");
        Article article = new Article(); article.setId("article"); article.setTitle("제목"); article.setContent("<p>본문</p>"); article.setCategory(allowed);
        when(categories.findAllBySiteIdAndExposed("site", true, Sort.unsorted())).thenReturn(List.of(allowed, denied));
        when(permissions.hasPermission("allowed", BasePermission.READ)).thenReturn(true);
        when(principal.getAuthentication()).thenReturn(new UsernamePasswordAuthenticationToken("user", ""));
        when(articles.findActivity(eq("user"), eq(List.of("allowed")), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(article)));
        ActivityService service = new ActivityService(articles, replies, votes, categories, permissions, principal);
        var page = service.find("site", "articles", PageRequest.of(0, 100));
        assertThat(page.getContent()).singleElement().satisfies(item -> {
            assertThat(item.title()).isEqualTo("제목");
            assertThat(item.preview()).isEqualTo("본문");
            assertThat(item.categoryName()).isEqualTo("게시판");
        });
        verify(articles).findActivity(eq("user"), eq(List.of("allowed")), argThat(p -> p.getPageSize() == 50));
    }

    @Test
    void 읽을_수_있는_카테고리가_없으면_빈_결과를_반환한다() {
        ArticleRepository articles = mock(ArticleRepository.class);
        CategoryRepository categories = mock(CategoryRepository.class);
        when(categories.findAllBySiteIdAndExposed("site", true, Sort.unsorted())).thenReturn(List.of());
        ActivityService service = new ActivityService(articles, mock(ReplyRepository.class), mock(VoteRepository.class), categories,
                mock(CategoryPermissionEvaluator.class), mock(PrincipalProvider.class));
        assertThat(service.find("site", "articles", PageRequest.of(0, 10))).isEmpty();
        verifyNoInteractions(articles);
    }
}
