package kr.me.seesaw.context;

import kr.me.seesaw.response.CategoryResponse;

public interface CategoryContext {

    CategoryResponse getCategory(String id);

}
