package kr.me.seesaw.api.category;

import kr.me.seesaw.api.category.dto.CategoryResponse;

public interface CategoryContext {

    CategoryResponse getCategory(String id);

}
