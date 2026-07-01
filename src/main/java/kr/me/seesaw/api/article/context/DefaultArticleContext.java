package kr.me.seesaw.api.article.context;

import kr.me.seesaw.api.article.ArticleContext;
import kr.me.seesaw.api.article.ArticleService;
import kr.me.seesaw.api.article.dto.ArticleResponse;
import kr.me.seesaw.api.article.dto.SearchArticlesRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.RequestScope;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequestScope
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Component("articleContext")
public class DefaultArticleContext implements ArticleContext {

    private final ArticleService articleService;

    private final Map<String, ArticleResponse> articleCache = new ConcurrentHashMap<>();

    private final Map<String, ArticleResponse> articleAggregationCache = new ConcurrentHashMap<>();

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
        return articleCache.computeIfAbsent(id, articleService::find);
    }

    @Override
    public ArticleResponse getArticleAggregation(String id) {
        return articleAggregationCache.computeIfAbsent(id, articleService::getArticleAggregation);
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
