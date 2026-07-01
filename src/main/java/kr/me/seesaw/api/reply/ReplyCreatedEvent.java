package kr.me.seesaw.api.reply;

public record ReplyCreatedEvent(String articleId, String replyId, String content) {

}
