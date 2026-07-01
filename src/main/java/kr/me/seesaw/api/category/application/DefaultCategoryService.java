package kr.me.seesaw.api.category.application;

import kr.me.seesaw.api.category.CategoryService;
import kr.me.seesaw.api.category.dto.CreateCategoryRequest;
import kr.me.seesaw.api.category.dto.MoveCategoryRequest;
import kr.me.seesaw.api.category.dto.UpdateCategoryRequest;
import kr.me.seesaw.core.support.hierarchy.HierarchicalFactory;
import kr.me.seesaw.core.domain.category.Category;
import kr.me.seesaw.core.domain.site.Site;
import kr.me.seesaw.api.category.dto.CategoryResponse;
import kr.me.seesaw.core.domain.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

@Transactional
@RequiredArgsConstructor
@Service
public class DefaultCategoryService implements CategoryService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponse createCategory(CreateCategoryRequest command) {
        logger.info("카테고리 생성: command={}", command);
        Category category = new Category();
        category.setName(command.getName());
        category.setDescription(command.getDescription());
        category.setType(command.getType());
        category.setSiteExposed(command.isSiteExposed());
        category.setSiteExposedOrder(command.getSiteExposedOrder());
        category.setExposed(command.isExposed());
        category.setSequence(command.getSequence());

        Site site = new Site();
        site.setId(command.getSiteId());
        category.setSite(site);

        if (command.getParentId() != null) {
            Category parent = new Category();
            parent.setId(command.getParentId());
            category.setParent(parent);
        }

        Category savedCategory = categoryRepository.save(category);
        return new CategoryResponse(savedCategory);
    }

    @Transactional(readOnly = true)
    @Override
    public CategoryResponse getCategoryById(String id) {
        logger.debug("카테고리 조회: id={}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 카테고리가 존재하지 않습니다. id: " + id));
        return new CategoryResponse(category);
    }

    @Override
    public CategoryResponse updateCategory(String id, UpdateCategoryRequest command) {
        logger.info("카테고리 수정: id={}, command={}", id, command);
        Category category = categoryRepository.getReferenceById(id);
        category.setName(command.getName());
        category.setDescription(command.getDescription());
        category.setType(command.getType());
        category.setSiteExposed(command.isSiteExposed());
        category.setSiteExposedOrder(command.getSiteExposedOrder());
        category.setExposed(command.isExposed());
        category.setSequence(command.getSequence());

        if (category.getSite() == null) {
            category.setSite(new Site());
        }
        category.getSite().setId(command.getSiteId());

        if (command.getParentId() != null) {
            Category parent = new Category();
            parent.setId(command.getParentId());
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        Category updatedCategory = categoryRepository.save(category);
        categoryRepository.flush();
        return new CategoryResponse(updatedCategory);
    }

    @Override
    public void deleteCategoryById(String id) {
        logger.info("카테고리 삭제: id={}", id);
        categoryRepository.deleteById(id);
    }

    @Override
    public List<CategoryResponse> getCategoriesBySiteId(String siteId) {
        logger.debug("사이트별 카테고리 목록 조회: siteId={}", siteId);
        Sort sort = Sort.by(Sort.Direction.ASC, "sequence");
        Collection<Category> categories = categoryRepository.findAllBySiteId(siteId, sort);
        List<CategoryResponse> models = categories.stream()
                .map(CategoryResponse::new)
                .toList();
        return HierarchicalFactory.build(models);
    }

    @Override
    public CategoryResponse moveCategory(String id, MoveCategoryRequest command) {
        logger.info("카테고리 이동: id={}, command={}", id, command);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 카테고리가 존재하지 않습니다. id: " + id));
        category.setSequence(command.getSequence());
        if (command.getParentId() != null) {
            Category parent = new Category();
            parent.setId(command.getParentId());
            category.setParent(parent);
        } else {
            category.setParent(null);
        }
        Category updatedCategory = categoryRepository.save(category);
        return new CategoryResponse(updatedCategory);
    }

}
