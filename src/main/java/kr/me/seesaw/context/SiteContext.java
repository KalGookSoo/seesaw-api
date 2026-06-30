package kr.me.seesaw.context;

import kr.me.seesaw.response.CategoryResponse;
import kr.me.seesaw.response.SiteResponse;

import java.util.List;
import java.util.Map;

public interface SiteContext {

    SiteResponse getSite();

    Map<String, CategoryResponse> getAllCategories();

    List<CategoryResponse> getNestedCategories();

}
