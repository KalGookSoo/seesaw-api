package kr.me.seesaw.api.article.context;

import kr.me.seesaw.api.article.ArticlePermissionContext;
import kr.me.seesaw.api.article.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequestScope
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Component("articlePermissionContext")
public class DefaultArticlePermissionContext implements ArticlePermissionContext {

    private final Map<String, Boolean> ownerCache = new ConcurrentHashMap<>();

    private final ArticleService articleService;

    @Override
    public boolean isOwner(String id, String username) {
        final String key = String.format("%s:%s", id, username);
        return ownerCache.computeIfAbsent(key, ignored -> articleService.isOwner(id, username));
    }

}
