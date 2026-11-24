package kr.me.seesaw.api.notification.application;

import kr.me.seesaw.api.notification.NotificationSubscriptionService;
import kr.me.seesaw.core.domain.category.Category;
import kr.me.seesaw.core.domain.category.CategoryRepository;
import kr.me.seesaw.core.domain.notification.NotificationSubscription;
import kr.me.seesaw.core.domain.notification.NotificationSubscriptionRepository;
import kr.me.seesaw.core.domain.site.Site;
import kr.me.seesaw.core.domain.site.SiteRepository;
import kr.me.seesaw.core.domain.user.User;
import kr.me.seesaw.core.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class DefaultNotificationSubscriptionService implements NotificationSubscriptionService {

    private final NotificationSubscriptionRepository subscriptionRepository;

    private final SiteRepository siteRepository;

    private final CategoryRepository categoryRepository;

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public boolean isSubscribed(String siteId, String username, String categoryId) {
        return subscriptionRepository.findBySiteIdAndUserUsernameAndCategoryId(siteId, username, categoryId)
                .map(NotificationSubscription::isEnabled)
                .orElse(false);
    }

    @Override
    public void subscribe(String siteId, String username, String categoryId) {
        Site site = siteRepository.findById(siteId).orElseThrow();
        Category category = categoryRepository.findById(categoryId).orElseThrow();
        if (!siteId.equals(category.getSiteId())) {
            throw new IllegalArgumentException("게시판이 사이트에 속하지 않습니다.");
        }
        User user = userRepository.findByUsername(username).orElseThrow();
        NotificationSubscription subscription = subscriptionRepository.findBySiteIdAndUserUsernameAndCategoryId(siteId, username, categoryId)
                .orElseGet(NotificationSubscription::new);
        subscription.setSite(site);
        subscription.setUser(user);
        subscription.setCategory(category);
        subscription.enable();
        subscriptionRepository.save(subscription);
    }

    @Override
    public void unsubscribe(String siteId, String username, String categoryId) {
        subscriptionRepository.findBySiteIdAndUserUsernameAndCategoryId(siteId, username, categoryId)
                .ifPresent(subscription -> {
                    subscription.disable();
                    subscriptionRepository.save(subscription);
                });
    }

}
