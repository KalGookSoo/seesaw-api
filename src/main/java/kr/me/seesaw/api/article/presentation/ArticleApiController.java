package kr.me.seesaw.api.article.presentation;

import jakarta.validation.Valid;
import kr.me.seesaw.api.article.dto.CreateArticleRequest;
import kr.me.seesaw.api.article.dto.MoveArticleRequest;
import kr.me.seesaw.api.article.dto.UpdateArticleRequest;
import kr.me.seesaw.api.article.ArticleContext;
import kr.me.seesaw.core.support.message.CmsMessageSource;
import kr.me.seesaw.api.article.dto.ArticleResponse;
import kr.me.seesaw.api.article.dto.SearchArticlesRequest;
import kr.me.seesaw.api.article.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/articles")
public class ArticleApiController {

    private final CmsMessageSource messageSource;

    private final ArticleContext articleContext;

    private final ArticleService articleService;

    @GetMapping
    public ResponseEntity<PagedModel<ArticleResponse>> getArticles(
            @PageableDefault(size = 8, sort = "article.createdDate", direction = Sort.Direction.DESC) Pageable pageable,
            @ModelAttribute("search") SearchArticlesRequest search
    ) {
        Page<ArticleResponse> page = articleContext.findAll(pageable, search);
        return ResponseEntity.ok(new PagedModel<>(page));
    }

    @PreAuthorize("isAuthenticated() and (hasAnyRole('ADMIN', 'MANAGER') or @categoryPermissionEvaluator.hasPermission(#categoryId, T(org.springframework.security.acls.domain.BasePermission).READ))")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, ArticleResponse>> getArticle(@PathVariable("id") String id, @RequestParam("categoryId") String categoryId) {
        ArticleResponse article = articleContext.getArticleAggregation(id);
        return ResponseEntity.ok(Map.of("article", article));
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
    public ResponseEntity<String> update(@PathVariable String id, @Valid UpdateArticleRequest command) throws IOException {
        articleService.update(id, command);
        String message = messageSource.getMessage("command.success.update");
        return ResponseEntity.ok(message);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @articlePermissionContext.isOwner(#id, authentication.name)")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
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
    public ResponseEntity<String> move(@PathVariable String id, @Valid MoveArticleRequest command) {
        articleService.move(id, command);
        String message = messageSource.getMessage("command.success.update");
        return ResponseEntity.ok(message);
    }

}
