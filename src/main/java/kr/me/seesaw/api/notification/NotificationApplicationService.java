package kr.me.seesaw.api.notification;

public interface NotificationApplicationService {

    void createForArticleCreated(String categoryId, String articleId, String title, String content, String actorUsername, String sourceEventId);

    void createForReplyCreated(String articleId, String replyId, String content, String actorUsername, String sourceEventId);

    void createForReport(String siteId, String title, String content, String actorUsername, String sourceEventId);

    void createForInquiry(String siteId, String title, String content, String actorUsername, String sourceEventId);

}
