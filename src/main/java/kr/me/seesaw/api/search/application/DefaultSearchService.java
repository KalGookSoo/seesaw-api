package kr.me.seesaw.api.search.application;

import kr.me.seesaw.api.search.dto.SearchArticleResponse;
import kr.me.seesaw.api.search.dto.SearchCategoryResponse;
import kr.me.seesaw.api.search.dto.SearchResponse;
import kr.me.seesaw.core.domain.article.Article;
import kr.me.seesaw.core.domain.article.ArticleRepository;
import kr.me.seesaw.core.domain.category.Category;
import kr.me.seesaw.core.domain.category.CategoryRepository;
import kr.me.seesaw.core.domain.site.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultSearchService implements SearchService {

    private static final int PREVIEW_LENGTH = 160;

    private final CategoryRepository categoryRepository;
    private final ArticleRepository articleRepository;
    private final SiteRepository siteRepository;

    @Transactional(readOnly = true)
    @Override
    public SearchResponse search(String siteId, String keyword, int limit) {
        String normalizedKeyword = keyword.trim();
        siteRepository.findById(siteId)
                .orElseThrow(() -> new java.util.NoSuchElementException("사이트를 찾을 수 없습니다. siteId: " + siteId));
        PageRequest pageRequest = PageRequest.of(0, limit);

        List<SearchCategoryResponse> categories = categoryRepository.search(siteId, normalizedKeyword, pageRequest)
                .stream()
                .map(this::toCategoryResponse)
                .toList();
        List<SearchArticleResponse> articles = articleRepository.search(siteId, normalizedKeyword, pageRequest)
                .stream()
                .map(this::toArticleResponse)
                .toList();

        return new SearchResponse(normalizedKeyword, categories, articles);
    }

    private SearchCategoryResponse toCategoryResponse(Category category) {
        String url = category.getType() == null || category.getType().name().equals("NONE")
                ? "/"
                : String.format("/articles?categoryType=%s&categoryId=%s", category.getType(), category.getId());
        return new SearchCategoryResponse(category.getId(), category.getName(), category.getDescription(), category.getType(), url);
    }

    private SearchArticleResponse toArticleResponse(Article article) {
        String plainContent = Jsoup.parse(article.getContent() == null ? "" : article.getContent()).text();
        String preview = plainContent.length() > PREVIEW_LENGTH
                ? plainContent.substring(0, PREVIEW_LENGTH) + "…"
                : plainContent;
        String url = String.format("/articles/%s?categoryId=%s", article.getId(), article.getCategoryId());
        return new SearchArticleResponse(article.getId(), article.getTitle(), preview, article.getCategoryId(), article.getCreatedDate(), url);
    }
}
