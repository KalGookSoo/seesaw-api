package kr.me.seesaw.controller;

import jakarta.validation.Valid;
import kr.me.seesaw.command.CreateCategoryCommand;
import kr.me.seesaw.command.MoveCategoryCommand;
import kr.me.seesaw.command.UpdateCategoryCommand;
import kr.me.seesaw.model.CategoryModel;
import kr.me.seesaw.service.CategoryService;
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
    public ResponseEntity<Map<String, List<CategoryModel>>> getCategories(@PathVariable("siteId") String siteId) {
        List<CategoryModel> models = categoryService.getCategoriesBySiteId(siteId);
        return ResponseEntity.ok(Map.of("categories", models));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, CategoryModel>> getCategoryById(@PathVariable("id") String id) {
        CategoryModel model = categoryService.getCategoryById(id);
        return ResponseEntity.ok(Map.of("category", model));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    public ResponseEntity<Map<String, CategoryModel>> createCategory(@Valid @RequestBody CreateCategoryCommand command) {
        CategoryModel category = categoryService.createCategory(command);
        return ResponseEntity.ok(Map.of("category", category));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, CategoryModel>> updateCategory(@PathVariable("id") String id, @Valid @RequestBody UpdateCategoryCommand command) {
        CategoryModel model = categoryService.updateCategory(id, command);
        return ResponseEntity.ok(Map.of("category", model));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/{id}/move")
    public ResponseEntity<Map<String, CategoryModel>> moveCategory(@PathVariable("id") String id, @Valid @RequestBody MoveCategoryCommand command) {
        CategoryModel model = categoryService.moveCategory(id, command);
        return ResponseEntity.ok(Map.of("category", model));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String id) {
        categoryService.deleteCategoryById(id);
        return ResponseEntity.ok().build();
    }
}
