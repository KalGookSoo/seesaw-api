package kr.me.seesaw.api.calendar.application;

import kr.me.seesaw.api.article.ArticleService;
import kr.me.seesaw.api.calendar.EventWebService;
import kr.me.seesaw.api.article.dto.CreateArticleRequest;
import kr.me.seesaw.api.article.dto.UpdateArticleRequest;
import kr.me.seesaw.core.domain.article.Article;
import kr.me.seesaw.core.domain.event.VEvent;
import kr.me.seesaw.core.domain.article.ArticleType;
import kr.me.seesaw.api.calendar.dto.CreateEventRequest;
import kr.me.seesaw.api.calendar.dto.UpdateEventRequest;
import kr.me.seesaw.api.calendar.dto.VEventResponse;
import kr.me.seesaw.api.calendar.dto.SearchEventsRequest;
import kr.me.seesaw.api.article.dto.ArticleResponse;
import kr.me.seesaw.core.domain.article.ArticleRepository;
import kr.me.seesaw.core.domain.event.EventRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Transactional
@RequiredArgsConstructor
@Service
public class DefaultEventWebService implements EventWebService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final EventRepository eventRepository;

    private final ArticleService articleService;

    private final ArticleRepository articleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<VEventResponse> findAll(SearchEventsRequest request) {
        logger.debug("이벤트 목록 조회: request={}", request);
        return eventRepository.findAll(request.categoryId(), request.start(), request.end(), request.query()).stream()
                .map(VEventResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VEventResponse find(String id) {
        logger.debug("이벤트 상세 조회: id={}", id);
        VEvent event = eventRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Event not found with id: " + id));
        VEventResponse model = new VEventResponse(event);

        // Article 상세 정보(첨부파일, 댓글 등)를 포함하기 위해 ArticleResponse 상세 조회를 활용할 수 있음
        ArticleResponse articleAggregation = articleService.getArticleAggregation(event.getArticleId());
        model.setArticle(articleAggregation);

        return model;
    }

    @Override
    @Transactional
    public VEventResponse create(CreateEventRequest command) throws IOException {
        logger.info("이벤트 생성: command={}", command);

        // 1. Article 생성
        CreateArticleRequest articleCommand = new CreateArticleRequest();
        articleCommand.setCategoryId(command.getCategoryId());
        articleCommand.setTitle(command.getTitle());
        articleCommand.setContent(command.getContent());
        articleCommand.setType(ArticleType.HTML);
        articleCommand.setFixed(false);
        articleCommand.setFixedOrder(null);
        articleCommand.setMultipartFiles(command.getMultipartFiles());
        articleCommand.setInlineImages(command.getInlineImages());

        ArticleResponse articleModel = articleService.create(articleCommand);
        Article article = articleRepository.getReferenceById(articleModel.getId());

        // 2. VEvent 생성 및 Article 연동
        VEvent event = new VEvent();
        event.setArticle(article);
        event.setDtStart(command.getDtStart());
        event.setDtEnd(command.getDtEnd());
        event.setSummary(command.getTitle());
        event.setLocation(command.getLocation());
        event.setStatus(command.getStatus());
        event.setTzid(command.getTzid());
        event.setDtStamp(Instant.now());

        eventRepository.save(event);

        VEventResponse model = new VEventResponse(event);
        model.setArticle(articleModel);
        model.setDescription(articleModel.getContent());
        return model;
    }

    @Override
    @Transactional
    public VEventResponse update(String id, UpdateEventRequest command) throws IOException {
        logger.info("이벤트 수정: id={}, command={}", id, command);
        VEvent event = eventRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Event not found with id: " + id));

        // 1. Article 수정
        UpdateArticleRequest articleCommand = new UpdateArticleRequest();
        articleCommand.setCategoryId(command.getCategoryId() != null ? command.getCategoryId() : event.getArticle().getCategory().getId());
        articleCommand.setTitle(command.getTitle());
        articleCommand.setContent(command.getContent());
        articleCommand.setType(ArticleType.HTML);
        articleCommand.setFixed(false);
        articleCommand.setFixedOrder(null);
        articleCommand.setMultipartFiles(command.getMultipartFiles());
        articleCommand.setInlineImages(command.getInlineImages());

        ArticleResponse articleModel = articleService.update(event.getArticleId(), articleCommand);

        // 2. VEvent 수정
        event.setDtStart(command.getDtStart());
        event.setDtEnd(command.getDtEnd());
        event.setSummary(command.getTitle());
        event.setLocation(command.getLocation());
        if (command.getStatus() != null) {
            event.setStatus(command.getStatus());
        }
        if (command.getTzid() != null) {
            event.setTzid(command.getTzid());
        }
        event.setDtStamp(Instant.now());

        VEvent updatedEvent = eventRepository.update(event);

        VEventResponse model = new VEventResponse(updatedEvent);
        model.setArticle(articleModel);
        model.setDescription(articleModel.getContent());
        return model;
    }

    @Override
    @Transactional
    public void delete(String id) {
        logger.info("이벤트 삭제: id={}", id);
        VEvent event = eventRepository.getReferenceById(id);
        eventRepository.delete(event);
        articleService.delete(event.getArticleId());
    }

}
