package kr.me.seesaw.api.article.event;

public record ArticleCreatedEvent(String categoryId, String articleId, String title, String content, String actorUsername) {

    public ArticleCreatedEvent(String categoryId, String articleId, String title, String content) {
        this(categoryId, articleId, title, content, null);
    }

    public String sourceEventId() {
        return "article-created:" + articleId;
    }

}
