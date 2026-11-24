package kr.me.seesaw.api.notification.event;

public record InquiryCreatedEvent(String siteId, String title, String content, String actorUsername, String sourceEventId) {
}
