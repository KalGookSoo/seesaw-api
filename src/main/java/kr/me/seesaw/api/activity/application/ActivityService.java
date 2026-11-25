package kr.me.seesaw.api.activity.application;

import java.util.List;
import kr.me.seesaw.api.activity.dto.ActivityResponse;
import kr.me.seesaw.api.category.CategoryPermissionEvaluator;
import kr.me.seesaw.core.domain.article.Article;
import kr.me.seesaw.core.domain.article.ArticleRepository;
import kr.me.seesaw.core.domain.category.Category;
import kr.me.seesaw.core.domain.category.CategoryRepository;
import kr.me.seesaw.core.domain.reply.Reply;
import kr.me.seesaw.core.domain.reply.ReplyRepository;
import kr.me.seesaw.core.domain.vote.Vote;
import kr.me.seesaw.core.domain.vote.VoteRepository;
import kr.me.seesaw.core.support.authentication.PrincipalProvider;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityService {
    private final ArticleRepository articleRepository;
    private final ReplyRepository replyRepository;
    private final VoteRepository voteRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryPermissionEvaluator categoryPermissionEvaluator;
    private final PrincipalProvider principalProvider;

    public Page<ActivityResponse> find(String siteId, String type, Pageable request) {
        Pageable pageable = PageRequest.of(request.getPageNumber(), Math.min(request.getPageSize(), 50),
                Sort.by(Sort.Order.desc("createdDate"), Sort.Order.desc("id")));
        List<String> categories = categoryRepository.findAllBySiteIdAndExposed(siteId, true, Sort.unsorted()).stream()
                .filter(category -> categoryPermissionEvaluator.hasPermission(category.getId(), BasePermission.READ))
                .map(Category::getId).toList();
        if (categories.isEmpty()) return new PageImpl<>(List.of(), pageable, 0);
        String username = principalProvider.getAuthentication().getName();
        return switch (type) {
            case "articles" -> articleRepository.findActivity(username, categories, pageable).map(article -> articleResponse(article, article.getCreatedDate()));
            case "replies" -> replyRepository.findActivity(username, categories, pageable).map(reply -> replyResponse(reply, reply.getCreatedDate()));
            case "likes" -> voteRepository.findActivity(username, categories, pageable).map(this::voteResponse);
            default -> throw new IllegalArgumentException("지원하지 않는 활동 유형입니다.");
        };
    }

    private ActivityResponse articleResponse(Article article, java.time.LocalDateTime date) {
        return response(article.getId(), "ARTICLE", article, article.getContent(), date);
    }

    private ActivityResponse replyResponse(Reply reply, java.time.LocalDateTime date) {
        return response(reply.getId(), "REPLY", reply.getArticle(), reply.getContent(), date);
    }

    private ActivityResponse voteResponse(Vote vote) {
        return articleRepository.findById(vote.getReferenceId())
                .map(article -> articleResponse(article, vote.getCreatedDate()))
                .orElseGet(() -> replyResponse(replyRepository.findById(vote.getReferenceId()).orElseThrow(), vote.getCreatedDate()));
    }

    private ActivityResponse response(String id, String type, Article article, String content, java.time.LocalDateTime date) {
        String text = Jsoup.parse(content == null ? "" : content).text();
        String preview = text.length() > 160 ? text.substring(0, 160) + "…" : text;
        Category category = article.getCategory();
        return new ActivityResponse(id, type, article.getId(), article.getTitle(), preview, category.getId(), category.getName(),
                article.getType() == null ? null : article.getType().getDescription(), date);
    }
}
