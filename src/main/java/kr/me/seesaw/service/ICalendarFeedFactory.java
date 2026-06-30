package kr.me.seesaw.service;

import kr.me.seesaw.config.SeesawProperties;
import kr.me.seesaw.domain.Article;
import kr.me.seesaw.domain.VEvent;
import kr.me.seesaw.domain.vo.RecurrenceRule;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ICalendarFeedFactory {

    private static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    private static final DateTimeFormatter UTC_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
            .withZone(ZoneOffset.UTC);

    private final SeesawProperties properties;

    public String create(List<VEvent> events, String categoryId) {
        List<String> lines = new ArrayList<>();
        lines.add("BEGIN:VCALENDAR");
        lines.add("VERSION:2.0");
        lines.add("PRODID:-//Seesaw//Calendar//KO");
        lines.add("CALSCALE:GREGORIAN");
        lines.add("METHOD:PUBLISH");
        lines.add("X-WR-CALNAME:" + escape(createCalendarName(categoryId)));
        lines.add("X-WR-TIMEZONE:Asia/Seoul");
        events.stream()
                .map(this::createEvent)
                .flatMap(List::stream)
                .forEach(lines::add);
        lines.add("END:VCALENDAR");
        return fold(String.join("\r\n", lines)) + "\r\n";
    }

    private String createCalendarName(String categoryId) {
        return StringUtils.hasText(categoryId) ? "Seesaw " + categoryId : "Seesaw Calendar";
    }

    private List<String> createEvent(VEvent event) {
        String tzid = StringUtils.hasText(event.getTzid()) ? event.getTzid() : "Asia/Seoul";
        Article article = event.getArticle();
        String summary = StringUtils.hasText(event.getSummary()) ? event.getSummary() : article.getTitle();

        List<String> lines = new ArrayList<>();
        lines.add("BEGIN:VEVENT");
        lines.add("UID:" + escape(event.getUid() + "@" + ServletUriComponentsBuilder.fromCurrentContextPath().toUriString()));
        lines.add("SEQUENCE:" + Optional.ofNullable(event.getSequence()).orElse(0));
        addInstant(lines, "DTSTAMP", Optional.ofNullable(event.getDtStamp()).orElse(Instant.now()));
        addInstant(lines, "CREATED", event.getCreated());
        addInstant(lines, "LAST-MODIFIED", event.getLastModified());
        lines.add("DTSTART;TZID=" + tzid + ":" + format(event.getDtStart()));
        if (event.getDtEnd() != null) {
            lines.add("DTEND;TZID=" + tzid + ":" + format(event.getDtEnd()));
        }
        lines.add("SUMMARY:" + escape(summary));
        lines.add("DESCRIPTION:" + escape(toPlainText(article.getContent())));
        if (StringUtils.hasText(event.getLocation())) {
            lines.add("LOCATION:" + escape(event.getLocation()));
        }
        if (event.getStatus() != null) {
            lines.add("STATUS:" + event.getStatus().name());
        }
        String rrule = createRecurrenceRule(event.getRrule());
        if (StringUtils.hasText(rrule)) {
            lines.add(rrule);
        }
        lines.add("END:VEVENT");
        return lines;
    }

    private String createRecurrenceRule(RecurrenceRule rrule) {
        if (rrule == null || rrule.getFreq() == null) {
            return "";
        }

        List<String> parts = new ArrayList<>();
        parts.add("FREQ=" + rrule.getFreq().name());
        if (rrule.getInterval() != null) {
            parts.add("INTERVAL=" + rrule.getInterval());
        }
        if (rrule.getUntil() != null) {
            parts.add("UNTIL=" + format(rrule.getUntil()));
        }
        if (rrule.getCount() != null) {
            parts.add("COUNT=" + rrule.getCount());
        }
        if (StringUtils.hasText(rrule.getByDay())) {
            parts.add("BYDAY=" + rrule.getByDay());
        }
        if (StringUtils.hasText(rrule.getByMonth())) {
            parts.add("BYMONTH=" + rrule.getByMonth());
        }
        if (StringUtils.hasText(rrule.getByMonthDay())) {
            parts.add("BYMONTHDAY=" + rrule.getByMonthDay());
        }
        if (rrule.getWkst() != null) {
            parts.add("WKST=" + toIcalendarDay(rrule.getWkst()));
        }
        return "RRULE:" + String.join(";", parts);
    }

    private void addInstant(List<String> lines, String name, Instant instant) {
        if (instant != null) {
            lines.add(name + ":" + UTC_DATE_TIME_FORMATTER.format(instant));
        }
    }

    private String format(LocalDateTime dateTime) {
        return dateTime.format(LOCAL_DATE_TIME_FORMATTER);
    }

    private String toIcalendarDay(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case SUNDAY -> "SU";
            case MONDAY -> "MO";
            case TUESDAY -> "TU";
            case WEDNESDAY -> "WE";
            case THURSDAY -> "TH";
            case FRIDAY -> "FR";
            case SATURDAY -> "SA";
        };
    }

    private String toPlainText(String html) {
        return StringUtils.hasText(html) ? Jsoup.parse(html).text() : "";
    }

    private String escape(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\r\n", "\\n")
                .replace("\n", "\\n");
    }

    private String fold(String value) {
        StringBuilder builder = new StringBuilder();
        for (String line : value.split("\\r\\n", -1)) {
            while (line.length() > 75) {
                builder.append(line, 0, 75).append("\r\n ");
                line = line.substring(75);
            }
            builder.append(line).append("\r\n");
        }
        return builder.toString().stripTrailing();
    }

}
