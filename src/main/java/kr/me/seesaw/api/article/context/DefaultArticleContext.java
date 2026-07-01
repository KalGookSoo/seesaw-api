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

    private final Map<String, Page<ArticleResponse>> categoryPageCache = new ConcurrentHashMap<>();

    private final Map<String, List<ArticleResponse>> fixedArticlesCache = new ConcurrentHashMap<>();

    @Override
    public Page<ArticleResponse> findAll(Pageable pageable, SearchArticlesRequest search) {
        return articleService.findAll(pageable, search);
    }

    @Override
    public Page<ArticleResponse> findAllByCategoryId(String categoryId, Pageable pageable) {
        String key = categoryId + ":" + pageable.getPageNumber() + ":" + pageable.getPageSize() + ":" + pageable.getSort();
        return categoryPageCache.computeIfAbsent(key, ignored -> articleService.findAllByCategoryId(categoryId, pageable));
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
        String key = categoryId + ":" + fixed + ":" + sort;
        return fixedArticlesCache.computeIfAbsent(key, ignored -> articleService.getFixedArticles(categoryId, fixed, sort));
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
