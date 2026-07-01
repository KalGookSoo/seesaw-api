package kr.me.seesaw.api.article.event;

public record ArticleCreatedEvent(String categoryId, String articleId, String title, String content) {

}
