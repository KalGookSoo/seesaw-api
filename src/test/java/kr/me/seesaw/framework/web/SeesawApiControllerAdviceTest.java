package kr.me.seesaw.framework.web;

import kr.me.seesaw.api.category.CategoryNotExposedException;
import kr.me.seesaw.core.support.message.CmsMessageSource;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SeesawApiControllerAdviceTest {

    @Test
    void 비노출_카테고리_예외는_안내_메시지와_함께_접근거부로_응답한다() {
        CmsMessageSource messageSource = mock(CmsMessageSource.class);
        SeesawApiControllerAdvice advice = new SeesawApiControllerAdvice(messageSource);
        String message = "비공개 카테고리의 게시글은 조회할 수 없습니다";
        when(messageSource.getMessage("error.category.not.exposed")).thenReturn(message);

        ResponseEntity<Map<String, String>> response = advice.handleCategoryNotExposedException(
                new CategoryNotExposedException(UUID.randomUUID().toString())
        );

        assertEquals(403, response.getStatusCode().value());
        assertEquals(Map.of("message", message), response.getBody());
    }

}
