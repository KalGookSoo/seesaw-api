package kr.me.seesaw.context;

import kr.me.seesaw.response.ArticleResponse;
import kr.me.seesaw.request.search.SearchArticlesRequest;
import kr.me.seesaw.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service("articleContext")
public class DefaultArticleContext implements ArticleContext {

    private final ArticleService articleService;

    @Override
    public Page<ArticleResponse> findAll(Pageable pageable, SearchArticlesRequest search) {
        return articleService.findAll(pageable, search);
    }

    @Override
    public Page<ArticleResponse> findAllByCategoryId(String categoryId, Pageable pageable) {
        return articleService.findAllByCategoryId(categoryId, pageable);
    }

    @Override
    public ArticleResponse find(String id) {
        return articleService.find(id);
    }

    @Override
    public ArticleResponse getArticleAggregation(String id) {
        return articleService.getArticleAggregation(id);
    }

    @Override
    public List<ArticleResponse> getFixedArticles(String categoryId, boolean fixed, Sort sort) {
        return articleService.getFixedArticles(categoryId, fixed, sort);
    }

    @Nullable
    @Override
    public ArticleResponse getPreviousArticle(SearchArticlesRequest search, LocalDateTime createdDate) {
        return articleService.getPreviousArticle(search, createdDate);
    }

    @Nullable
    @Override
    public ArticleResponse getNextArticle(SearchArticlesRequest search, LocalDateTime createdDate) {
        return articleService.getNextArticle(search, createdDate);
    }

}
