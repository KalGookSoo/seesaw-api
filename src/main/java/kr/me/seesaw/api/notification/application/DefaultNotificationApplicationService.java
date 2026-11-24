package kr.me.seesaw.api.notification.application;

import kr.me.seesaw.api.notification.NotificationApplicationService;
import kr.me.seesaw.core.domain.article.Article;
import kr.me.seesaw.core.domain.article.ArticleRepository;
import kr.me.seesaw.core.domain.category.Category;
import kr.me.seesaw.core.domain.category.CategoryRepository;
import kr.me.seesaw.core.domain.notification.Notification;
import kr.me.seesaw.core.domain.notification.NotificationRepository;
import kr.me.seesaw.core.domain.notification.NotificationSubscription;
import kr.me.seesaw.core.domain.notification.NotificationSubscriptionRepository;
import kr.me.seesaw.core.domain.notification.NotificationTargetType;
import kr.me.seesaw.core.domain.notification.NotificationType;
import kr.me.seesaw.core.domain.mapping.RoleMapping;
import kr.me.seesaw.core.domain.mapping.RoleMappingRepository;
import kr.me.seesaw.core.domain.role.RoleName;
import kr.me.seesaw.core.domain.reply.ReplyRepository;
import kr.me.seesaw.core.domain.site.Site;
import kr.me.seesaw.core.domain.site.SiteRepository;
import kr.me.seesaw.core.domain.user.User;
import kr.me.seesaw.core.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.NoSuchElementException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
@RequiredArgsConstructor
@Service
public class DefaultNotificationApplicationService implements NotificationApplicationService {

    private final NotificationRepository notificationRepository;

    private final NotificationSubscriptionRepository subscriptionRepository;

    private final CategoryRepository categoryRepository;

    private final ArticleRepository articleRepository;

    private final ReplyRepository replyRepository;

    private final UserRepository userRepository;

    private final SiteRepository siteRepository;

    private final RoleMappingRepository roleMappingRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void createForArticleCreated(String categoryId, String articleId, String title, String content, String actorUsername, String sourceEventId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NoSuchElementException("카테고리를 찾을 수 없습니다: " + categoryId));
        String siteId = category.getSiteId();
        String message = summarize(content);
        for (NotificationSubscription subscription : subscriptionRepository.findAllBySiteIdAndCategoryIdAndEnabledTrue(siteId, categoryId)) {
            User recipient = subscription.getUser();
            if (isSameUser(recipient, actorUsername)) {
                continue;
            }
            saveIfAbsent(category.getSite(), recipient, actorUsername, NotificationType.ARTICLE_CREATED,
                    "새 게시글이 등록되었습니다.", message, NotificationTargetType.ARTICLE, articleId,
                    articleUrl(articleId, categoryId), sourceEventId);
        }
    }

    @Override
    public void createForReplyCreated(String articleId, String replyId, String content, String actorUsername, String sourceEventId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다: " + articleId));
        String recipientUsername = article.getCreatedBy();
        if (!StringUtils.hasText(recipientUsername) || recipientUsername.equals(actorUsername)) {
            return;
        }
        User recipient = userRepository.findByUsername(recipientUsername)
                .orElseThrow(() -> new NoSuchElementException("수신자 계정을 찾을 수 없습니다: " + recipientUsername));
        Category category = article.getCategory();
        saveIfAbsent(category.getSite(), recipient, actorUsername, NotificationType.REPLY_CREATED,
                "내 게시글에 댓글이 등록되었습니다.", summarize(content), NotificationTargetType.REPLY, replyId,
                articleUrl(articleId, category.getId()) + "#" + replyId, sourceEventId);
    }

    @Override
    public void createForReport(String siteId, String title, String content, String actorUsername, String sourceEventId) {
        createForSiteManagers(siteId, actorUsername, NotificationType.REPORT_CREATED, "새 신고가 접수되었습니다.",
                title + "\n" + summarize(content), NotificationTargetType.REPORT, sourceEventId);
    }

    @Override
    public void createForInquiry(String siteId, String title, String content, String actorUsername, String sourceEventId) {
        createForSiteManagers(siteId, actorUsername, NotificationType.INQUIRY_CREATED, "새 문의가 접수되었습니다.",
                title + "\n" + summarize(content), NotificationTargetType.INQUIRY, sourceEventId);
    }

    private void createForSiteManagers(String siteId, String actorUsername, NotificationType type, String title,
                                       String message, NotificationTargetType targetType, String sourceEventId) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new NoSuchElementException("사이트를 찾을 수 없습니다: " + siteId));
        Set<String> roleNames = Arrays.stream(RoleName.values())
                .filter(role -> role == RoleName.ROLE_ADMIN || role == RoleName.ROLE_MANAGER)
                .map(RoleName::name)
                .collect(Collectors.toSet());
        Collection<RoleMapping> mappings = roleMappingRepository.findAllBySiteIdAndRoleNameIn(siteId, roleNames);
        Set<String> userIds = mappings.stream().map(RoleMapping::getUserId).collect(Collectors.toSet());
        for (User recipient : userRepository.findAllByIdIn(userIds)) {
            if (isSameUser(recipient, actorUsername)) {
                continue;
            }
            saveIfAbsent(site, recipient, actorUsername, type, title, message, targetType, null, null, sourceEventId);
        }
    }

    private void saveIfAbsent(Site site, User recipient, String actorUsername, NotificationType type,
                               String title, String message, NotificationTargetType targetType, String targetId,
                               String targetUrl, String sourceEventId) {
        if (notificationRepository.findByRecipientUsernameAndSourceEventIdAndType(recipient.getUsername(), sourceEventId, type).isPresent()) {
            return;
        }
        Notification notification = new Notification();
        notification.setSite(site);
        notification.setRecipient(recipient);
        notification.setActorUsername(actorUsername);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setTargetType(targetType);
        notification.setTargetId(targetId);
        notification.setTargetUrl(targetUrl);
        notification.setSourceEventId(sourceEventId);
        Notification savedNotification = notificationRepository.save(notification);
        eventPublisher.publishEvent(new kr.me.seesaw.api.notification.event.NotificationCreatedEvent(
                savedNotification.getId(), savedNotification.getSiteId(), savedNotification.getRecipient().getUsername(),
                savedNotification.getType(), savedNotification.getTitle(), savedNotification.getMessage(), savedNotification.getTargetUrl()
        ));
    }

    private boolean isSameUser(User recipient, String actorUsername) {
        return StringUtils.hasText(actorUsername) && actorUsername.equals(recipient.getUsername());
    }

    private String articleUrl(String articleId, String categoryId) {
        return "/articles/%s?categoryId=%s".formatted(articleId, categoryId);
    }

    private String summarize(String content) {
        if (!StringUtils.hasText(content)) {
            return "새로운 활동이 등록되었습니다.";
        }
        return content.length() > 120 ? content.substring(0, 120) + "..." : content;
    }

}
