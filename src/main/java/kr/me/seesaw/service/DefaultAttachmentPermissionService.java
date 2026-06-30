package kr.me.seesaw.service;

import kr.me.seesaw.core.authentication.PrincipalProvider;
import kr.me.seesaw.domain.Article;
import kr.me.seesaw.domain.Attachment;
import kr.me.seesaw.domain.Category;
import kr.me.seesaw.repository.ArticleRepository;
import kr.me.seesaw.repository.AttachmentRepository;
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
@Service
public class DefaultAttachmentPermissionService implements AttachmentPermissionService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final PrincipalProvider principalProvider;

    private final ArticleRepository articleRepository;

    private final AttachmentRepository attachmentRepository;

    private final PermissionEvaluator permissionEvaluator;

    @Override
    public boolean hasPermission(String attachmentId, Permission permission) {
        logger.debug("인가를 검증합니다. attachmentId: {}, permission: {}", attachmentId, permission);
        Attachment attachment = attachmentRepository.getReferenceById(attachmentId);
        Article article = articleRepository.getReferenceById(attachment.getReferenceId());
        Category category = article.getCategory();
        return permissionEvaluator.hasPermission(principalProvider.getAuthentication(), category.getId(), Category.class.getCanonicalName(), permission);
    }

    @Override
    public boolean isOwner(String attachmentId) {
        logger.info("첨부파일 소유자 확인: attachmentId={}", attachmentId);
        Authentication authentication = principalProvider.getAuthentication();
        if (authentication == null) {
            return false;
        }
        String username = authentication.getName();
        Attachment attachment = attachmentRepository.getReferenceById(attachmentId);
        return attachment.getCreatedBy().equals(username);
    }

}
