package kr.me.seesaw.service;

import kr.me.seesaw.dto.request.SearchEventsRequest;

public interface ICalendarFeedService {

    String createFeed(SearchEventsRequest request);

}
