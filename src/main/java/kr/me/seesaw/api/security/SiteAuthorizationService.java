package kr.me.seesaw.api.security;

import kr.me.seesaw.core.domain.article.Article;
import kr.me.seesaw.core.domain.article.ArticleRepository;
import kr.me.seesaw.core.domain.category.Category;
import kr.me.seesaw.core.domain.category.CategoryRepository;
import kr.me.seesaw.core.domain.mapping.RoleMapping;
import kr.me.seesaw.core.domain.mapping.RoleMappingRepository;
import kr.me.seesaw.core.domain.attachment.AttachmentRepository;
import kr.me.seesaw.core.domain.event.EventRepository;
import kr.me.seesaw.core.domain.reply.ReplyRepository;
import kr.me.seesaw.core.domain.role.RoleName;
import kr.me.seesaw.core.domain.site.SiteRepository;
import kr.me.seesaw.core.domain.user.User;
import kr.me.seesaw.core.domain.user.UserRepository;
import kr.me.seesaw.core.support.authentication.PrincipalProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service("siteAuthorizationService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SiteAuthorizationService {

    private final PrincipalProvider principalProvider;
    private final UserRepository userRepository;
    private final RoleMappingRepository roleMappingRepository;
    private final CategoryRepository categoryRepository;
    private final ArticleRepository articleRepository;
    private final SiteRepository siteRepository;
    private final AttachmentRepository attachmentRepository;
    private final ReplyRepository replyRepository;
    private final EventRepository eventRepository;

    public boolean hasRoleForSite(String siteId) {
        return hasRoleForSite(siteId, List.of(RoleName.ROLE_USER, RoleName.ROLE_MANAGER, RoleName.ROLE_ADMIN));
    }

    public boolean hasManagerRoleForSite(String siteId) {
        return hasRoleForSite(siteId, List.of(RoleName.ROLE_ADMIN, RoleName.ROLE_MANAGER));
    }

    public boolean hasManagerRoleForCategory(String categoryId) {
        return categoryRepository.findById(categoryId)
                .map(Category::getSiteId)
                .map(this::hasManagerRoleForSite)
                .orElse(false);
    }

    public boolean hasManagerRoleForArticle(String articleId) {
        return articleRepository.findById(articleId)
                .map(Article::getCategoryId)
                .map(categoryRepository::findById)
                .flatMap(category -> category.map(Category::getSiteId))
                .map(this::hasManagerRoleForSite)
                .orElse(false);
    }

    public boolean hasManagerRoleForReply(String replyId) {
        return replyRepository.findById(replyId)
                .map(reply -> reply.getArticle().getCategoryId())
                .map(categoryRepository::findById)
                .flatMap(category -> category.map(Category::getSiteId))
                .map(this::hasManagerRoleForSite)
                .orElse(false);
    }

    public boolean hasManagerRoleForAttachment(String attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .map(kr.me.seesaw.core.domain.attachment.Attachment::getReferenceId)
                .map(this::hasManagerRoleForArticle)
                .orElse(false);
    }

    public boolean hasManagerRoleForEvent(String eventId) {
        return eventRepository.findById(eventId)
                .map(event -> event.getArticle().getCategoryId())
                .map(categoryRepository::findById)
                .flatMap(category -> category.map(Category::getSiteId))
                .map(this::hasManagerRoleForSite)
                .orElse(false);
    }

    public boolean hasManagerRoleForAllArticles(Collection<String> articleIds) {
        return articleIds != null && articleIds.stream().allMatch(this::hasManagerRoleForArticle);
    }

    private boolean hasRoleForSite(String siteId, Collection<RoleName> roleNames) {
        if (siteId == null || siteRepository.findById(siteId).isEmpty()) {
            return false;
        }

        Authentication authentication = principalProvider.getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null) {
            return false;
        }

        return roleMappingRepository.findAllByUserIdAndSiteId(user.getId(), siteId)
                .stream()
                .map(RoleMapping::getRole)
                .filter(Objects::nonNull)
                .anyMatch(role -> roleNames.stream().anyMatch(role::has));
    }
}
