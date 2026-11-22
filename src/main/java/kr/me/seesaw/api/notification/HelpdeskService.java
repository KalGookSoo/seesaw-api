package kr.me.seesaw.api.notification;

public interface HelpdeskService {

    void sendToHelpdesk(String siteId, String title, String content);

}
