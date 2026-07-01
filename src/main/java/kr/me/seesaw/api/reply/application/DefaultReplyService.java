package kr.me.seesaw.api.reply.application;

import kr.me.seesaw.api.reply.ReplyService;
import kr.me.seesaw.api.reply.dto.CreateReplyRequest;
import kr.me.seesaw.api.reply.dto.UpdateReplyRequest;
import kr.me.seesaw.core.domain.article.Article;
import kr.me.seesaw.core.domain.reply.Reply;
import kr.me.seesaw.api.reply.ReplyCreatedEvent;
import kr.me.seesaw.api.reply.dto.ReplyResponse;
import kr.me.seesaw.core.domain.article.ArticleRepository;
import kr.me.seesaw.core.domain.reply.ReplyRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Transactional
@RequiredArgsConstructor
@Service
public class DefaultReplyService implements ReplyService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ReplyRepository replyRepository;

    private final ArticleRepository articleRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    @Override
    public ReplyResponse find(String id) {
        logger.debug("댓글 조회: id={}", id);
        Reply reply = replyRepository.findById(id).orElseThrow(NoSuchElementException::new);
        return new ReplyResponse(reply);
    }

    @Override
    public ReplyResponse create(CreateReplyRequest command) {
        logger.info("댓글 생성: command={}", command);
        Reply reply = new Reply();
        Article article = articleRepository.getReferenceById(command.getArticleId());
        reply.setArticle(article);
        reply.setContent(command.getContent());
        reply.setExposed(command.isExposed());

        Reply savedReply = replyRepository.save(reply);
        ReplyCreatedEvent event = new ReplyCreatedEvent(article.getId(), savedReply.getId(), Jsoup.parse(command.getContent()).text());
        eventPublisher.publishEvent(event);
        return new ReplyResponse(savedReply);
    }

    @Override
    public ReplyResponse update(String id, UpdateReplyRequest command) {
        logger.info("댓글 수정: id={}, command={}", id, command);
        Reply reply = replyRepository.getReferenceById(id);
        reply.setContent(command.getContent());
        reply.setExposed(command.isExposed());

        Reply updatedReply = replyRepository.save(reply);
        return new ReplyResponse(updatedReply);
    }

    @Override
    public void delete(String id) {
        logger.info("댓글 삭제: id={}", id);
        Reply reply = replyRepository.getReferenceById(id);
        replyRepository.delete(reply);
    }

}
