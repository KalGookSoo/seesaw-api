package kr.me.seesaw.api.article.event;

public record ArticleViewedEvent(String articleId, String viewerIp, String viewerUsername) {

}
