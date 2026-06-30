package kr.me.seesaw.context;

import kr.me.seesaw.core.hierarchy.HierarchicalFactory;
import kr.me.seesaw.domain.Category;
import kr.me.seesaw.domain.Site;
import kr.me.seesaw.response.ArticleResponse;
import kr.me.seesaw.response.AttachmentResponse;
import kr.me.seesaw.response.BaseResponse;
import kr.me.seesaw.response.CategoryResponse;
import kr.me.seesaw.response.SiteResponse;
import kr.me.seesaw.repository.ArticleQueryRepository;
import kr.me.seesaw.repository.AttachmentRepository;
import kr.me.seesaw.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.RequestScope;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequestScope
@Transactional
@RequiredArgsConstructor
@Service("siteContext")
public class CurrentSiteContext implements SiteContext {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final SiteRepository siteRepository;

    private final AttachmentRepository attachmentRepository;

    private final ArticleQueryRepository articleQueryRepository;

    private final Environment environment;

    private SiteResponse site;

    private Map<String, CategoryResponse> allCategories;

    private List<CategoryResponse> nestedCategories;

    @Transactional(readOnly = true)
    @Override
    public SiteResponse getSite() {
        if (this.site == null) {
            String applicationName = environment.getProperty("spring.application.name");
            String domainName = applicationName + ".seesaw.me.kr";
            this.site = getSiteContext(domainName);
        }
        return this.site;
    }

    private SiteResponse getSiteContext(String domainName) {
        logger.debug("사이트 컨텍스트 조회: domainName={}", domainName);
        Site siteEntity = siteRepository.findByDomainName(domainName)
                .orElseThrow(() -> new NoSuchElementException("사이트를 찾을 수 없습니다. domainName: " + domainName));
        SiteResponse siteModel = new SiteResponse(siteEntity);

        logger.debug("프로필 이미지, 배경 이미지 조인");
        attachmentRepository.findAllByReferenceIdIn(Collections.singletonList(siteEntity.getId()))
                .stream()
                .map(AttachmentResponse::new)
                .forEach(siteModel::addAttachment);

        logger.debug("카테고리 조인");
        siteEntity.getCategories()
                .stream()
                .filter(Category::isExposed)
                .sorted(Comparator.comparing(Category::getSequence))
                .map(CategoryResponse::new)
                .forEach(siteModel::addCategory);

        logger.debug("최근 7일 게시글 조인");
        List<String> categoryIds = siteModel.getCategories()
                .stream()
                .map(CategoryResponse::getId)
                .toList();
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);

        List<ArticleResponse> articles = articleQueryRepository.findAllByCategoryId(categoryIds, cutoffDate)
                .stream()
                .map(ArticleResponse::new)
                .sorted(Comparator.comparing(BaseResponse::getCreatedDate))
                .toList();
        siteModel.getCategories()
                .forEach(categoryModel -> categoryModel.joinArticles(articles));

        Map<String, CategoryResponse> allCategories = siteModel.getCategories()
                .stream()
                .collect(Collectors.toMap(CategoryResponse::getId, Function.identity()));

        logger.debug("최근 게시글을 병합하여 상위 카테고리 게시글에 바인딩");
        siteModel.getCategories()
                .stream()
                .filter(CategoryResponse::isRoot)
                .forEach(categoryModel -> articles.stream()
                        .filter(article -> categoryModel.getId().equals(allCategories.get(article.getCategoryId()).getParentId()))
                        .forEach(categoryModel::addRecentArticle));

        logger.debug("최근 게시글을 해당 카테고리에도 바인딩");
        articles.forEach(article -> {
            CategoryResponse category = allCategories.get(article.getCategoryId());
            if (category != null) {
                category.addRecentArticle(article);
            }
        });

        return siteModel;
    }

    @Override
    public Map<String, CategoryResponse> getAllCategories() {
        if (this.allCategories == null) {
            this.allCategories = getSite().getCategories().stream()
                    .collect(Collectors.toMap(CategoryResponse::getId, Function.identity(), (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        }
        return this.allCategories;
    }

    @Override
    public List<CategoryResponse> getNestedCategories() {
        if (this.nestedCategories == null) {
            this.nestedCategories = HierarchicalFactory.build(getSite().getCategories());
        }
        return this.nestedCategories;
    }

}
