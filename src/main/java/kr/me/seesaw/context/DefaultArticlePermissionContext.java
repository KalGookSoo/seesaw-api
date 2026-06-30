package kr.me.seesaw.context;

import kr.me.seesaw.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service("articlePermissionContext")
public class DefaultArticlePermissionContext implements ArticlePermissionContext {

    private final ArticleService articleService;

    @Override
    public boolean isOwner(String id, String username) {
        return articleService.isOwner(id, username);
    }

}
