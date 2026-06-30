package kr.me.seesaw.service;

import kr.me.seesaw.domain.VEvent;
import kr.me.seesaw.dto.request.SearchEventsRequest;
import kr.me.seesaw.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultICalendarFeedService implements ICalendarFeedService {

    private final EventRepository eventRepository;

    private final ICalendarFeedFactory iCalendarFeedFactory;

    @Override
    @Transactional(readOnly = true)
    public String createFeed(SearchEventsRequest request) {
        final String categoryId = request.categoryId();
        List<VEvent> events = eventRepository.findAll(categoryId, request.start(), request.end(), request.query());
        return iCalendarFeedFactory.create(events, categoryId);
    }

}
