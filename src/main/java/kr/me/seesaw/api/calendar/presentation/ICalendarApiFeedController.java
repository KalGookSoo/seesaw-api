package kr.me.seesaw.api.calendar.presentation;

import kr.me.seesaw.api.calendar.dto.SearchEventsRequest;
import kr.me.seesaw.api.calendar.ICalendarFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/calendars")
public class ICalendarApiFeedController {

    private static final MediaType TEXT_CALENDAR_MEDIA_TYPE = MediaType.valueOf("text/calendar;charset=UTF-8");

    private final ICalendarFeedService iCalendarFeedService;

    @GetMapping(value = "/categories/{categoryId}/events.ics", produces = "text/calendar;charset=UTF-8")
    public ResponseEntity<String> getEvents(
            @PathVariable String categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        final SearchEventsRequest request = SearchEventsRequest.builder()
                .categoryId(categoryId)
                .start(start)
                .end(end)
                .build();

        return ResponseEntity.ok()
                .contentType(TEXT_CALENDAR_MEDIA_TYPE)
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)).cachePublic())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename("seesaw-calendar.ics", StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(iCalendarFeedService.createFeed(request));
    }

}
