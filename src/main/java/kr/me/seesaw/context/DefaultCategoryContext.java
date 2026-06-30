package kr.me.seesaw.context;

import kr.me.seesaw.response.CategoryResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.util.NoSuchElementException;

@RequestScope
@RequiredArgsConstructor
@Service("categoryContext")
public class DefaultCategoryContext implements CategoryContext {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final SiteContext siteContext;

    @Override
    public CategoryResponse getCategory(String id) {
        logger.debug("카테고리 조회: id={}", id);
        CategoryResponse category = siteContext.getAllCategories().get(id);
        if (category == null) {
            throw new NoSuchElementException("해당 카테고리가 존재하지 않습니다. id: " + id);
        }
        return category;
    }

}
