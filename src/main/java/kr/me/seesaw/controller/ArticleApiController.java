package kr.me.seesaw.controller;

import jakarta.validation.Valid;
import kr.me.seesaw.command.CreateArticleCommand;
import kr.me.seesaw.command.UpdateArticleCommand;
import kr.me.seesaw.message.CmsMessageSource;
import kr.me.seesaw.model.ArticleModel;
import kr.me.seesaw.search.ArticleSearch;
import kr.me.seesaw.service.ArticleService;
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

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/articles")
public class ArticleApiController {

    private final CmsMessageSource messageSource;

    private final ArticleService articleService;

    @GetMapping
    public ResponseEntity<PagedModel<ArticleModel>> getArticles(
            @PageableDefault(size = 8, sort = "article.createdDate", direction = Sort.Direction.DESC) Pageable pageable,
            @ModelAttribute("search") ArticleSearch search
    ) {
        Page<ArticleModel> page = articleService.findAll(pageable, search);
        return ResponseEntity.ok(new PagedModel<>(page));
    }

    @PreAuthorize("isAuthenticated() and (hasAnyRole('ADMIN', 'MANAGER') or hasPermission(#command.categoryId, 'kr.me.seesaw.domain.Category', T(org.springframework.security.acls.domain.BasePermission).CREATE))")
    @PostMapping
    public ResponseEntity<String> create(@Valid CreateArticleCommand command) throws IOException {
        articleService.create(command);
        String message = messageSource.getMessage("command.success.create");
        return ResponseEntity.ok(message);
    }

    @PreAuthorize("@defaultArticleService.isOwner(#id, authentication.name)")
    @PostMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable String id, @Valid UpdateArticleCommand command) throws IOException {
        articleService.update(id, command);
        String message = messageSource.getMessage("command.success.update");
        return ResponseEntity.ok(message);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @defaultArticleService.isOwner(#id, authentication.name)")
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

}
