package kr.me.seesaw.api.reply.application;

import kr.me.seesaw.api.reply.ReplyPermissionService;
import kr.me.seesaw.core.support.authentication.PrincipalProvider;
import kr.me.seesaw.core.domain.article.Article;
import kr.me.seesaw.core.domain.category.Category;
import kr.me.seesaw.core.domain.reply.Reply;
import kr.me.seesaw.core.domain.reply.ReplyRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service("replyPermissionService")
public class DefaultReplyPermissionService implements ReplyPermissionService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final PrincipalProvider principalProvider;

    private final ReplyRepository replyRepository;

    private final PermissionEvaluator permissionEvaluator;

    @Override
    public boolean hasPermission(String replyId, Permission permission) {
        logger.debug("인가를 검증합니다. replyId: {}, permission: {}", replyId, permission);
        Reply reply = replyRepository.getReferenceById(replyId);
        Article article = reply.getArticle();
        Category category = article.getCategory();
        return permissionEvaluator.hasPermission(principalProvider.getAuthentication(), category.getId(), Category.class.getCanonicalName(), permission);
    }

    @Override
    public boolean isOwner(String replyId) {
        logger.info("댓글 소유자 확인: replyId={}", replyId);
        Authentication authentication = principalProvider.getAuthentication();
        if (authentication == null) {
            return false;
        }
        String username = authentication.getName();
        Reply reply = replyRepository.getReferenceById(replyId);
        return reply.getCreatedBy().equals(username);
    }

}
