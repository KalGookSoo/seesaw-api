package kr.me.seesaw.api.site;

import kr.me.seesaw.api.category.dto.CategoryResponse;
import kr.me.seesaw.api.site.dto.SiteResponse;

import java.util.List;
import java.util.Map;

public interface SiteContext {

    SiteResponse getSite();

    SiteResponse getSiteContext();

    Map<String, CategoryResponse> getAllCategories();

    List<CategoryResponse> getNestedCategories();

}
