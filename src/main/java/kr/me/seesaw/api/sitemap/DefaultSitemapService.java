package kr.me.seesaw.api.sitemap;

import kr.me.seesaw.api.article.dto.ArticleResponse;
import kr.me.seesaw.api.category.dto.CategoryResponse;
import kr.me.seesaw.core.domain.article.ArticleRepository;
import kr.me.seesaw.core.domain.category.Category;
import kr.me.seesaw.core.domain.category.CategoryRepository;
import kr.me.seesaw.core.domain.category.CategoryType;
import kr.me.seesaw.core.domain.site.Site;
import kr.me.seesaw.core.domain.site.SiteRepository;
import kr.me.seesaw.core.support.dto.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class DefaultSitemapService implements SitemapService {

    private static final DateTimeFormatter LAST_MODIFIED_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final SiteRepository siteRepository;

    private final CategoryRepository categoryRepository;

    private final ArticleRepository articleRepository;

    @Override
    public String getSitemap(String origin, String domainName) {
        Site site = siteRepository.findByDomainName(domainName)
                .orElseThrow(() -> new NoSuchElementException("사이트를 찾을 수 없습니다. domainName: " + domainName));
        List<CategoryResponse> categories = getExposedCategories(site.getId());
        List<ArticleResponse> articles = getExposedArticles(categories);
        Map<String, CategoryResponse> categoryMap = toCategoryMap(categories);
        Map<String, SitemapUrl> urls = new LinkedHashMap<>();

        putUrl(urls, origin, LocalDateTime.now(), "1.00");
        categories.forEach(category -> putUrl(urls, toCategoryUrl(origin, category), getLastModifiedDate(category), "0.80"));
        articles.forEach(article -> {
            CategoryResponse category = categoryMap.get(article.getCategoryId());
            if (category != null) {
                putUrl(urls, toArticleUrl(origin, article, category), getLastModifiedDate(article), "0.64");
            }
        });

        return toXml(urls.values());
    }

    private List<CategoryResponse> getExposedCategories(String siteId) {
        Sort sort = Sort.by(Sort.Order.asc("sequence"), Sort.Order.asc("siteExposedOrder"));
        Collection<Category> categories = categoryRepository.findAllBySiteId(siteId, sort);
        return categories.stream()
                .filter(Category::isExposed)
                .filter(category -> category.getType() != CategoryType.NONE)
                .map(CategoryResponse::new)
                .sorted(Comparator.comparing(CategoryResponse::getSequence, Comparator.nullsLast(Integer::compareTo)))
                .toList();
    }

    private List<ArticleResponse> getExposedArticles(List<CategoryResponse> categories) {
        List<String> categoryIds = categories.stream()
                .map(CategoryResponse::getId)
                .toList();
        if (categoryIds.isEmpty()) {
            return List.of();
        }

        Map<String, CategoryResponse> categoryMap = toCategoryMap(categories);
        Sort sort = Sort.by(Sort.Order.desc("createdDate"));
        return articleRepository.findAllByCategoryIdInAndExposed(categoryIds, true, sort)
                .stream()
                .filter(article -> categoryMap.containsKey(article.getCategoryId()))
                .map(ArticleResponse::new)
                .sorted(Comparator.comparing(BaseResponse::getCreatedDate, Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
                .toList();
    }

    private Map<String, CategoryResponse> toCategoryMap(List<CategoryResponse> categories) {
        return categories.stream()
                .collect(Collectors.toMap(CategoryResponse::getId, Function.identity()));
    }

    private String toCategoryUrl(String origin, CategoryResponse category) {
        return UriComponentsBuilder.fromUriString(origin)
                .path("/articles")
                .queryParam("categoryType", category.getType())
                .queryParam("categoryId", category.getId())
                .build()
                .toUriString();
    }

    private String toArticleUrl(String origin, ArticleResponse article, CategoryResponse category) {
        return UriComponentsBuilder.fromUriString(origin)
                .path("/articles/{articleId}")
                .queryParam("categoryType", category.getType())
                .queryParam("categoryId", category.getId())
                .buildAndExpand(article.getId())
                .toUriString();
    }

    private void putUrl(Map<String, SitemapUrl> urls, String location, LocalDateTime lastModifiedDate, String priority) {
        urls.putIfAbsent(location, new SitemapUrl(location, toLastModified(lastModifiedDate), priority));
    }

    private LocalDateTime getLastModifiedDate(CategoryResponse category) {
        return Objects.requireNonNullElse(category.getLastModifiedDate(), category.getCreatedDate());
    }

    private LocalDateTime getLastModifiedDate(ArticleResponse article) {
        return Objects.requireNonNullElse(article.getLastModifiedDate(), article.getCreatedDate());
    }

    private String toLastModified(LocalDateTime lastModifiedDate) {
        LocalDateTime value = Objects.requireNonNullElseGet(lastModifiedDate, LocalDateTime::now);
        OffsetDateTime offsetDateTime = value.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();
        return LAST_MODIFIED_FORMATTER.format(offsetDateTime);
    }

    private String toXml(Collection<SitemapUrl> urls) {
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        builder.append("<urlset\n");
        builder.append("      xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"\n");
        builder.append("      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        builder.append("      xsi:schemaLocation=\"http://www.sitemaps.org/schemas/sitemap/0.9\n");
        builder.append("            http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd\">\n");
        urls.forEach(url -> appendUrl(builder, url));
        builder.append("</urlset>\n");
        return builder.toString();
    }

    private void appendUrl(StringBuilder builder, SitemapUrl url) {
        builder.append("<url>\n");
        builder.append("  <loc>").append(escapeXml(url.location())).append("</loc>\n");
        builder.append("  <lastmod>").append(escapeXml(url.lastModified())).append("</lastmod>\n");
        builder.append("  <priority>").append(escapeXml(url.priority())).append("</priority>\n");
        builder.append("</url>\n");
    }

    private String escapeXml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private record SitemapUrl(String location, String lastModified, String priority) {
    }

}
