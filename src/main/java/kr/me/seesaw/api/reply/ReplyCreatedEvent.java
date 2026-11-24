package kr.me.seesaw.api.reply;

public record ReplyCreatedEvent(String articleId, String replyId, String content, String actorUsername) {

    public ReplyCreatedEvent(String articleId, String replyId, String content) {
        this(articleId, replyId, content, null);
    }

    public String sourceEventId() {
        return "reply-created:" + replyId;
    }

}
