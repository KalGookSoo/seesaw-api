package kr.me.seesaw.context;

import kr.me.seesaw.response.ArticleResponse;
import kr.me.seesaw.request.search.SearchArticlesRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.List;

public interface ArticleContext {

    Page<ArticleResponse> findAll(Pageable pageable, SearchArticlesRequest search);

    Page<ArticleResponse> findAllByCategoryId(String categoryId, Pageable pageable);

    ArticleResponse find(String id);

    ArticleResponse getArticleAggregation(String id);

    List<ArticleResponse> getFixedArticles(String categoryId, boolean fixed, Sort sort);

    @Nullable
    ArticleResponse getPreviousArticle(SearchArticlesRequest search, LocalDateTime createdDate);

    @Nullable
    ArticleResponse getNextArticle(SearchArticlesRequest search, LocalDateTime createdDate);

}
