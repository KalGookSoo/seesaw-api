package kr.me.seesaw.api.article.event;

import kr.me.seesaw.core.domain.article.Article;
import kr.me.seesaw.core.domain.view.View;
import kr.me.seesaw.core.domain.view.ViewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class ArticleViewEventListener {

    private final ViewRepository viewRepository;

    public ArticleViewEventListener(ViewRepository viewRepository) {
        this.viewRepository = viewRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onArticleViewed(ArticleViewedEvent event) {
        log.debug("게시글 조회 이벤트 처리: {}", event);
        View view = new View();
        Article article = new Article();
        article.setId(event.articleId());
        view.setArticle(article);
        viewRepository.save(view);
    }

}
