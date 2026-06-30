package kr.me.seesaw.service;

import kr.me.seesaw.request.CreateArticleRequest;
import kr.me.seesaw.request.MoveArticleRequest;
import kr.me.seesaw.request.UpdateArticleRequest;
import kr.me.seesaw.response.ArticleResponse;
import kr.me.seesaw.request.search.SearchArticlesRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public interface ArticleService {

    Page<ArticleResponse> findAll(Pageable pageable, SearchArticlesRequest search);

    Page<ArticleResponse> findAllByCategoryId(String categoryId, Pageable pageable);

    ArticleResponse find(String id);

    ArticleResponse getArticleAggregation(String id);

    ArticleResponse create(CreateArticleRequest command) throws IOException;

    ArticleResponse update(String id, UpdateArticleRequest command) throws IOException;

    ArticleResponse move(String id, MoveArticleRequest command);

    void delete(String id);

    void deleteAll(List<String> ids);

    boolean isOwner(String id, String username);

    List<ArticleResponse> getFixedArticles(String categoryId, boolean fixed, Sort sort);

    @Nullable
    ArticleResponse getPreviousArticle(SearchArticlesRequest search, LocalDateTime createdDate);

    @Nullable
    ArticleResponse getNextArticle(SearchArticlesRequest search, LocalDateTime createdDate);

}
