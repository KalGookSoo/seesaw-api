package kr.me.seesaw.controller;

import jakarta.validation.Valid;
import kr.me.seesaw.command.CreateCategoryCommand;
import kr.me.seesaw.model.CategoryModel;
import kr.me.seesaw.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CategoryApiController {
    public final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<Map<String, CategoryModel>> createCategory(@Valid @RequestBody CreateCategoryCommand command) {
        CategoryModel category = categoryService.createCategory(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("category", category));
    }


}
