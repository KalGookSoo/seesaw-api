package kr.me.seesaw.service;

import kr.me.seesaw.core.authentication.PrincipalProvider;
import kr.me.seesaw.domain.Article;
import kr.me.seesaw.domain.Category;
import kr.me.seesaw.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.model.Permission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class DefaultArticlePermissionService implements ArticlePermissionService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final PrincipalProvider principalProvider;

    private final ArticleRepository articleRepository;

    private final PermissionEvaluator permissionEvaluator;

    @Override
    public boolean hasPermission(String articleId, Permission permission) {
        logger.debug("인가를 검증합니다. articleId: {}, permission: {}", articleId, permission);
        Article article = articleRepository.getReferenceById(articleId);
        Category category = article.getCategory();
        return permissionEvaluator.hasPermission(principalProvider.getAuthentication(), category.getId(), Category.class.getCanonicalName(), permission);
    }

}
