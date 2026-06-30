package kr.me.seesaw.service;

import kr.me.seesaw.request.CreateCategoryRequest;
import kr.me.seesaw.request.MoveCategoryRequest;
import kr.me.seesaw.request.UpdateCategoryRequest;
import kr.me.seesaw.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CreateCategoryRequest command);

    CategoryResponse getCategoryById(String id);

    CategoryResponse updateCategory(String id, UpdateCategoryRequest command);

    void deleteCategoryById(String id);

    List<CategoryResponse> getCategoriesBySiteId(String siteId);

    CategoryResponse moveCategory(String id, MoveCategoryRequest command);

}
