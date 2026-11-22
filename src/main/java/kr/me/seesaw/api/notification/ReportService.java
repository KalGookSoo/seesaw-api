package kr.me.seesaw.api.notification;

public interface ReportService {

    void sendToReport(String siteId, String title, String content);

}
