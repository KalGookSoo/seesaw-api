package kr.me.seesaw.api.category.presentation;

import jakarta.validation.Valid;
import kr.me.seesaw.api.category.dto.CreateCategoryRequest;
import kr.me.seesaw.api.category.dto.MoveCategoryRequest;
import kr.me.seesaw.api.category.dto.UpdateCategoryRequest;
import kr.me.seesaw.api.category.dto.CategoryResponse;
import kr.me.seesaw.api.category.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryApiController {
    public final CategoryService categoryService;

    @GetMapping("/by-site-id/{siteId}")
    public ResponseEntity<Map<String, List<CategoryResponse>>> getCategories(@PathVariable("siteId") String siteId) {
        List<CategoryResponse> models = categoryService.getCategoriesBySiteId(siteId);
        return ResponseEntity.ok(Map.of("categories", models));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, CategoryResponse>> getCategoryById(@PathVariable("id") String id) {
        CategoryResponse model = categoryService.getCategoryById(id);
        return ResponseEntity.ok(Map.of("category", model));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    public ResponseEntity<Map<String, CategoryResponse>> createCategory(@Valid @RequestBody CreateCategoryRequest command) {
        CategoryResponse category = categoryService.createCategory(command);
        return ResponseEntity.ok(Map.of("category", category));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, CategoryResponse>> updateCategory(@PathVariable("id") String id, @Valid @RequestBody UpdateCategoryRequest command) {
        CategoryResponse model = categoryService.updateCategory(id, command);
        return ResponseEntity.ok(Map.of("category", model));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/{id}/move")
    public ResponseEntity<Map<String, CategoryResponse>> moveCategory(@PathVariable("id") String id, @Valid @RequestBody MoveCategoryRequest command) {
        CategoryResponse model = categoryService.moveCategory(id, command);
        return ResponseEntity.ok(Map.of("category", model));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String id) {
        categoryService.deleteCategoryById(id);
        return ResponseEntity.ok().build();
    }
}
