package kr.me.seesaw.api.site.context;

import kr.me.seesaw.api.article.dto.ArticleResponse;
import kr.me.seesaw.api.attachment.dto.AttachmentResponse;
import kr.me.seesaw.api.category.dto.CategoryResponse;
import kr.me.seesaw.api.site.SiteContext;
import kr.me.seesaw.api.site.dto.SiteResponse;
import kr.me.seesaw.core.domain.article.ArticleQueryRepository;
import kr.me.seesaw.core.domain.attachment.AttachmentRepository;
import kr.me.seesaw.core.domain.category.Category;
import kr.me.seesaw.core.domain.site.Site;
import kr.me.seesaw.core.domain.site.SiteRepository;
import kr.me.seesaw.core.support.dto.BaseResponse;
import kr.me.seesaw.core.support.hierarchy.HierarchicalFactory;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.RequestScope;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequestScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Component("siteContext")
public class CurrentSiteContext implements SiteContext {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final SiteRepository siteRepository;

    private final AttachmentRepository attachmentRepository;

    private final ArticleQueryRepository articleQueryRepository;

    private final Environment environment;

    private SiteResponse site;

    private SiteResponse siteContext;

    private List<CategoryResponse> nestedCategories;

    @Override
    public SiteResponse getSite() {
        if (site != null) {
            return site;
        }
        String applicationName = environment.getProperty("spring.application.name");
        String domainName = applicationName + ".seesaw.me.kr";
        site = siteRepository.findByDomainName(domainName)
                .map(SiteResponse::new)
                .orElseThrow(() -> new NoSuchElementException("사이트를 찾을 수 없습니다. domainName: " + domainName));
        return site;
    }

    @Override
    public SiteResponse getSiteContext() {
        if (siteContext != null) {
            return siteContext;
        }
        String applicationName = environment.getProperty("spring.application.name");
        String domainName = applicationName + ".seesaw.me.kr";

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

        siteContext = siteModel;
        site = siteModel;
        return siteContext;
    }

    public List<CategoryResponse> getNestedCategories(List<CategoryResponse> categories) {
        if (nestedCategories == null) {
            nestedCategories = List.copyOf(HierarchicalFactory.build(categories));
        }
        return nestedCategories;
    }

}
