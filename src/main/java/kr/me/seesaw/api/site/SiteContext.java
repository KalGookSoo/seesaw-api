package kr.me.seesaw.api.site;

import kr.me.seesaw.api.category.dto.CategoryResponse;
import kr.me.seesaw.api.site.dto.SiteResponse;

import java.util.List;

public interface SiteContext {

    SiteResponse getSite();

    SiteResponse getSiteContext();

    List<CategoryResponse> getNestedCategories(List<CategoryResponse> categories);

}
