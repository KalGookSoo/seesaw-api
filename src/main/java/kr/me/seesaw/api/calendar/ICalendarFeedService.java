package kr.me.seesaw.api.calendar;

import kr.me.seesaw.api.calendar.dto.SearchEventsRequest;

public interface ICalendarFeedService {

    String createFeed(SearchEventsRequest request);

}
