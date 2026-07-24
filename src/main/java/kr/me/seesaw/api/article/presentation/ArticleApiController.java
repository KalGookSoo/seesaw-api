package kr.me.seesaw.api.article.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import kr.me.seesaw.api.article.ArticleContext;
import kr.me.seesaw.api.article.ArticleService;
import kr.me.seesaw.api.article.dto.*;
import kr.me.seesaw.api.category.CategoryNotExposedException;
import kr.me.seesaw.api.category.CategoryService;
import kr.me.seesaw.api.category.dto.CategoryResponse;
import kr.me.seesaw.core.support.message.CmsMessageSource;
import kr.me.seesaw.core.support.pattern.PatternMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/api/articles")
public class ArticleApiController {

    private final CmsMessageSource messageSource;

    private final ArticleContext articleContext;

    private final ArticleService articleService;

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<PagedModel<ArticleResponse>> getArticles(
            @PageableDefault(size = 8, sort = "article.createdDate", direction = Sort.Direction.DESC) Pageable pageable,
            @Valid @ModelAttribute("search") SearchArticlesRequest search
    ) {
        Page<ArticleResponse> page = articleContext.findAll(pageable, search);
        return ResponseEntity.ok(new PagedModel<>(page));
    }

    @GetMapping("/static-content")
    public ResponseEntity<List<ArticleResponse>> getStaticContentArticles(
            @RequestParam @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String categoryId
    ) {
        Sort sort = Sort.by(Sort.Order.asc("fixedOrder"), Sort.Order.desc("createdDate"));
        Pageable pageable = Pageable.unpaged(sort);
        Page<ArticleResponse> page = articleContext.findAllByCategoryId(categoryId, pageable);
        return ResponseEntity.ok(page.getContent());
    }

    @GetMapping("/fixed")
    public ResponseEntity<List<ArticleResponse>> getFixedArticles(
            @RequestParam @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String categoryId
    ) {
        Sort sort = Sort.by(Sort.Order.asc("fixedOrder"), Sort.Order.desc("createdDate"));
        List<ArticleResponse> fixedArticles = articleContext.getFixedArticles(categoryId, true, sort);
        return ResponseEntity.ok(fixedArticles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleDetailResponse> getArticle(
            @PathVariable("id") @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String id,
            @Valid @ModelAttribute("search") SearchArticleDetailRequest request
    ) {
        ArticleResponse articleSummary = articleContext.find(id);
        CategoryResponse articleCategory = categoryService.getCategoryById(articleSummary.getCategoryId());
        if (!articleCategory.isExposed()) {
            throw new CategoryNotExposedException(articleCategory.getId());
        }

        ArticleResponse article = articleContext.getArticleAggregation(id);
        SearchArticlesRequest search = request.toSearchArticlesRequest();
        search.setCategoryId(article.getCategoryId());
        if (search.getCategoryType() == null) {
            search.setCategoryType(articleCategory.getType());
        }

        String previousArticleId = getExposedArticleId(articleContext.getPreviousArticle(search, article.getCreatedDate()));
        String nextArticleId = getExposedArticleId(articleContext.getNextArticle(search, article.getCreatedDate()));
        ArticleDetailResponse response = ArticleDetailResponse.builder()
                .article(article)
                .previousArticleId(previousArticleId)
                .nextArticleId(nextArticleId)
                .build();
        return ResponseEntity.ok(response);
    }

    private String getExposedArticleId(ArticleResponse article) {
        if (article == null) {
            return null;
        }
        CategoryResponse category = categoryService.getCategoryById(article.getCategoryId());
        return category.isExposed() ? article.getId() : null;
    }

    @PreAuthorize("isAuthenticated() and (hasAnyRole('ADMIN', 'MANAGER') or @categoryPermissionEvaluator.hasPermission(#command.categoryId, T(org.springframework.security.acls.domain.BasePermission).CREATE))")
    @PostMapping
    public ResponseEntity<String> create(@Valid CreateArticleRequest command) throws IOException {
        articleService.create(command);
        String message = messageSource.getMessage("command.success.create");
        return ResponseEntity.ok(message);
    }

    @PreAuthorize("@articlePermissionContext.isOwner(#id, authentication.name)")
    @PostMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String id, @Valid UpdateArticleRequest command) throws IOException {
        articleService.update(id, command);
        String message = messageSource.getMessage("command.success.update");
        return ResponseEntity.ok(message);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @articlePermissionContext.isOwner(#id, authentication.name)")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String id) {
        articleService.delete(id);
        String message = messageSource.getMessage("command.success.delete");
        return ResponseEntity.ok(message);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping
    public ResponseEntity<String> deleteAll(@RequestBody List<String> ids) {
        articleService.deleteAll(ids);
        String message = messageSource.getMessage("command.success.delete");
        return ResponseEntity.ok(message);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/{id}/move")
    public ResponseEntity<String> move(@PathVariable @NotBlank @Pattern(regexp = PatternMatcher.UUID_V4) String id, @Valid MoveArticleRequest command) {
        articleService.move(id, command);
        String message = messageSource.getMessage("command.success.update");
        return ResponseEntity.ok(message);
    }

}
