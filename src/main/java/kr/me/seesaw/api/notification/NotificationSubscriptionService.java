package kr.me.seesaw.api.notification;

public interface NotificationSubscriptionService {

    boolean isSubscribed(String siteId, String username, String categoryId);

    void subscribe(String siteId, String username, String categoryId);

    void unsubscribe(String siteId, String username, String categoryId);

}
