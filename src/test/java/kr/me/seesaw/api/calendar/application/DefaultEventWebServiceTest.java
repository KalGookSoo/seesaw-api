package kr.me.seesaw.api.calendar.application;

import kr.me.seesaw.api.article.ArticleService;
import kr.me.seesaw.api.article.dto.ArticleResponse;
import kr.me.seesaw.api.calendar.dto.UpdateEventRequest;
import kr.me.seesaw.core.domain.article.ArticleRepository;
import kr.me.seesaw.core.domain.event.EventRepository;
import kr.me.seesaw.core.domain.event.VEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.validation.DataBinder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DefaultEventWebServiceTest {

    @Test
    void 폼에서_상세_장소_생략과_빈_문자열을_구분한다() {
        UpdateEventRequest omitted = new UpdateEventRequest();
        new DataBinder(omitted).bind(new MutablePropertyValues());
        assertNull(omitted.getLocationDetail());

        UpdateEventRequest empty = new UpdateEventRequest();
        new DataBinder(empty).bind(new MutablePropertyValues(Map.of("locationDetail", "")));
        assertEquals("", empty.getLocationDetail());
    }

    @Test
    void 구버전_요청의_상세_장소는_유지하고_빈_값은_삭제한다() throws Exception {
        EventRepository eventRepository = mock(EventRepository.class);
        ArticleService articleService = mock(ArticleService.class);
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        VEvent event = mock(VEvent.class);
        when(eventRepository.findById("event")).thenReturn(Optional.of(event));
        when(eventRepository.update(event)).thenReturn(event);
        when(articleService.update(any(), any())).thenReturn(mock(ArticleResponse.class));
        DefaultEventWebService service = new DefaultEventWebService(eventRepository, articleService, articleRepository);

        UpdateEventRequest request = new UpdateEventRequest();
        request.setCategoryId("category");
        service.update("event", request);
        verify(event, never()).setLocationDetail(any());

        request.setLocationDetail("");
        service.update("event", request);
        verify(event).setLocationDetail(eq(null));

        request.setLocationDetail("  3층  ");
        service.update("event", request);
        verify(event).setLocationDetail("3층");
    }
}
