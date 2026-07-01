package kr.me.seesaw.api.calendar;

import kr.me.seesaw.api.calendar.dto.CreateEventRequest;
import kr.me.seesaw.api.calendar.dto.UpdateEventRequest;
import kr.me.seesaw.api.calendar.dto.VEventResponse;
import kr.me.seesaw.api.calendar.dto.SearchEventsRequest;

import java.io.IOException;
import java.util.List;

public interface EventWebService {

    List<VEventResponse> findAll(SearchEventsRequest request);

    VEventResponse find(String id);

    VEventResponse create(CreateEventRequest command) throws IOException;

    VEventResponse update(String id, UpdateEventRequest command) throws IOException;

    void delete(String id);

}
