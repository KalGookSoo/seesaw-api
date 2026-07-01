package kr.me.seesaw.api.category;

import kr.me.seesaw.api.category.dto.CreateCategoryRequest;
import kr.me.seesaw.api.category.dto.MoveCategoryRequest;
import kr.me.seesaw.api.category.dto.UpdateCategoryRequest;
import kr.me.seesaw.api.category.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CreateCategoryRequest command);

    CategoryResponse getCategoryById(String id);

    CategoryResponse updateCategory(String id, UpdateCategoryRequest command);

    void deleteCategoryById(String id);

    List<CategoryResponse> getCategoriesBySiteId(String siteId);

    CategoryResponse moveCategory(String id, MoveCategoryRequest command);

}
