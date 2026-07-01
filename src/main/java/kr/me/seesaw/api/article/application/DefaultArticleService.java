package kr.me.seesaw.api.article.application;

import kr.me.seesaw.api.article.ArticleQueryService;
import kr.me.seesaw.api.article.ArticleService;
import kr.me.seesaw.framework.context.properties.SeesawApiProperties;
import kr.me.seesaw.core.domain.article.Article;
import kr.me.seesaw.core.domain.article.ArticleQueryRepository;
import kr.me.seesaw.core.domain.article.ArticleRepository;
import kr.me.seesaw.core.domain.attachment.Attachment;
import kr.me.seesaw.core.domain.attachment.AttachmentRepository;
import kr.me.seesaw.core.domain.category.Category;
import kr.me.seesaw.core.domain.category.CategoryRepository;
import kr.me.seesaw.core.domain.reply.Reply;
import kr.me.seesaw.core.domain.reply.ReplyRepository;
import kr.me.seesaw.core.domain.view.View;
import kr.me.seesaw.core.domain.view.ViewRepository;
import kr.me.seesaw.core.support.audit.IpAddressExtractor;
import kr.me.seesaw.core.support.authentication.PrincipalProvider;
import kr.me.seesaw.core.domain.*;
import kr.me.seesaw.core.support.file.FileManager;
import kr.me.seesaw.api.article.event.ArticleCreatedEvent;
import kr.me.seesaw.api.article.event.ArticleViewedEvent;
import kr.me.seesaw.api.article.dto.CreateArticleRequest;
import kr.me.seesaw.api.article.dto.MoveArticleRequest;
import kr.me.seesaw.api.article.dto.UpdateArticleRequest;
import kr.me.seesaw.api.article.dto.SearchArticlesRequest;
import kr.me.seesaw.api.article.dto.ArticleResponse;
import kr.me.seesaw.api.attachment.dto.AttachmentResponse;
import kr.me.seesaw.api.reply.dto.ReplyResponse;
import kr.me.seesaw.api.view.dto.ViewResponse;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Transactional
@Service
@RequiredArgsConstructor
public class DefaultArticleService implements ArticleService, ArticleQueryService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final SeesawApiProperties seesawApiProperties;

    private final ArticleRepository articleRepository;

    private final ArticleQueryRepository articleQueryRepository;

    private final CategoryRepository categoryRepository;

    private final ReplyRepository replyRepository;

    private final ViewRepository viewRepository;

    private final AttachmentRepository attachmentRepository;

    private final FileManager fileManager;

    private final PrincipalProvider principalProvider;

    private final IpAddressExtractor ipAddressExtractor;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    @Override
    public Page<ArticleResponse> findAll(Pageable pageable, SearchArticlesRequest search) {
        logger.debug("게시글 전체 조회: pageable={}, search={}", pageable, search);
        String sort = pageable.getSort().isSorted() ? pageable.getSort().toString().replace(":", "") : Sort.Direction.DESC.name();

        List<Article> articles = articleQueryRepository.search(
                (int) pageable.getOffset(),
                pageable.getPageSize(),
                sort,
                search.getCategoryId(),
                search.getKeyField(),
                search.getKeyWord()
        );
        long count = articleQueryRepository.count(search.getCategoryId(), search.getKeyField(), search.getKeyWord());

        Page<Article> entityPage = new PageImpl<>(articles, pageable, count);
        Page<ArticleResponse> page = entityPage.map(ArticleResponse::new);

        // 페이지 요청이 아닐 경우 조인하지 않는다.
        if (pageable.isUnpaged()) {
            return page;
        }

        List<String> articleIds = page.getContent()
                .stream()
                .map(ArticleResponse::getId)
                .toList();

        List<ReplyResponse> replies = replyRepository.findAllByArticleIdIn(articleIds)
                .stream()
                .sorted(Comparator.comparing(BaseEntity::getCreatedDate))
                .map(ReplyResponse::new)
                .toList();
        page.getContent()
                .forEach(article -> article.joinReplies(replies));

        List<ViewResponse> views = viewRepository.findAllByArticleIdIn(articleIds)
                .stream()
                .sorted(Comparator.comparing(BaseEntity::getCreatedDate))
                .map(ViewResponse::new)
                .toList();
        page.getContent()
                .forEach(article -> article.joinViews(views));

        List<AttachmentResponse> attachments = attachmentRepository.findAllByReferenceIdIn(articleIds)
                .stream()
                .sorted(Comparator.comparing(BaseEntity::getCreatedDate))
                .map(AttachmentResponse::new)
                .toList();
        page.getContent()
                .forEach(article -> article.joinAttachments(attachments));
        return page;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ArticleResponse> findAllByCategoryId(String categoryId, Pageable pageable) {
        logger.debug("카테고리별 게시글 조회: categoryId={}, pageable={}", categoryId, pageable);
        Page<Article> entityPage = articleRepository.findAllByCategoryId(categoryId, pageable);
        Page<ArticleResponse> page = entityPage.map(ArticleResponse::new);

        // 페이지 요청이 아닐 경우 조인하지 않는다.
        if (pageable.isUnpaged()) {
            return page;
        }
        List<String> articleIds = page.getContent().stream().map(ArticleResponse::getId).toList();

        List<ReplyResponse> replies = replyRepository.findAllByArticleIdIn(articleIds)
                .stream()
                .sorted(Comparator.comparing(BaseEntity::getCreatedDate))
                .map(ReplyResponse::new)
                .toList();
        page.getContent().forEach(article -> article.joinReplies(replies));

        List<ViewResponse> views = viewRepository.findAllByArticleIdIn(articleIds)
                .stream()
                .sorted(Comparator.comparing(BaseEntity::getCreatedDate))
                .map(ViewResponse::new)
                .toList();
        page.getContent().forEach(article -> article.joinViews(views));

        List<AttachmentResponse> attachments = attachmentRepository.findAllByReferenceIdIn(articleIds)
                .stream()
                .sorted(Comparator.comparing(BaseEntity::getCreatedDate))
                .map(AttachmentResponse::new)
                .toList();
        page.getContent().forEach(article -> article.joinAttachments(attachments));
        return page;
    }

    @Transactional(readOnly = true)
    @Override
    public ArticleResponse find(String id) {
        logger.debug("게시글 상세 조회: id={}", id);
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 게시글이 존재하지 않습니다."));
        return new ArticleResponse(article);
    }

    @Transactional(readOnly = true)
    public ArticleResponse getArticleAggregation(String id) {
        logger.debug("게시글 애그리게이션 조회: id={}", id);
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 게시글이 존재하지 않습니다."));
        ArticleResponse model = new ArticleResponse(article);

        List<ReplyResponse> replies = replyRepository.findAllByArticleIdIn(Collections.singletonList(id))
                .stream()
                .sorted(Comparator.comparing(BaseEntity::getCreatedDate))
                .map(ReplyResponse::new)
                .toList();
        model.joinReplies(replies);

        List<ViewResponse> views = viewRepository.findAllByArticleIdIn(Collections.singletonList(id))
                .stream()
                .sorted(Comparator.comparing(BaseEntity::getCreatedDate))
                .map(ViewResponse::new)
                .toList();
        model.joinViews(views);

        List<AttachmentResponse> attachments = attachmentRepository.findAllByReferenceIdIn(Collections.singletonList(id))
                .stream()
                .sorted(Comparator.comparing(BaseEntity::getCreatedDate))
                .map(AttachmentResponse::new)
                .toList();
        model.joinAttachments(attachments);

        eventPublisher.publishEvent(new ArticleViewedEvent(id, ipAddressExtractor.getCurrentIp(), principalProvider.getAuthentication().getName()));

        return model;
    }

    @Override
    public ArticleResponse create(CreateArticleRequest command) throws IOException {
        logger.info("게시글 생성: command={}", command);

        // 생성될 게시글의 식별자를 참조하기위해 먼저 게시글을 저장한다.
        Article article = new Article();
        Category category = new Category();
        category.setId(command.getCategoryId());
        article.setCategory(category);
        article.setType(command.getType());
        article.setFixed(command.isFixed());
        article.setFixedOrder(command.getFixedOrder());
        article.setTitle(command.getTitle());
        article.setContent(command.getContent());

        Article savedArticle = articleRepository.save(article);

        Document document = Jsoup.parse(command.getContent());
        Iterator<Element> iterator = document.select("img[src*=\"blob:\"]").iterator();

        for (MultipartFile multipartFile : command.getInlineImages()) {
            Attachment attachment = new Attachment();
            attachment.setReferenceId(savedArticle.getId());
            attachment.setOriginalName(multipartFile.getOriginalFilename());
            attachment.setName(UUID.randomUUID() + "_" + attachment.getOriginalName());
            attachment.setPathName(Attachment.Type.INLINE_IMAGE.getPath());
            attachment.setMimeType(multipartFile.getContentType());
            attachment.setSize(multipartFile.getSize());

            fileManager.write(seesawApiProperties.getFilepath() + attachment.getPathName() + File.separator + attachment.getName(), multipartFile.getBytes());
            attachmentRepository.save(attachment);

            String url = "/api/attachments/" + attachment.getId();

            // images의 src를 첨부파일을 생성 후 "/api/attachments/{id}"로 치환한다.
            if (iterator.hasNext()) {
                Element element = iterator.next();
                element.attr("src", url);
            }
        }

        // 첨부파일
        for (MultipartFile multipartFile : command.getMultipartFiles()) {
            Attachment attachment = new Attachment();
            attachment.setReferenceId(savedArticle.getId());
            attachment.setOriginalName(multipartFile.getOriginalFilename());
            attachment.setName(UUID.randomUUID() + "_" + attachment.getOriginalName());
            attachment.setPathName(Attachment.Type.ATTACHMENT.getPath());
            attachment.setMimeType(multipartFile.getContentType());
            attachment.setSize(multipartFile.getSize());

            fileManager.write(seesawApiProperties.getFilepath() + attachment.getPathName() + File.separator + attachment.getName(), multipartFile.getBytes());
            attachmentRepository.save(attachment);
        }

        // 인라인이미지 링크를 첨부파일 API로 치환한 본문으로 재할당한다
        Safelist safelist = Safelist.relaxed().preserveRelativeLinks(true);
        article.setContent(Jsoup.clean(document.body().html(), "http://localhost", safelist));

        ArticleResponse model = new ArticleResponse(articleRepository.save(article));
        ArticleCreatedEvent event = new ArticleCreatedEvent(command.getCategoryId(), model.getId(), model.getTitle(), model.getPlainContent());
        eventPublisher.publishEvent(event);
        return model;
    }

    @Override
    public ArticleResponse update(String id, UpdateArticleRequest command) throws IOException {
        logger.info("게시글 수정: id={}, command={}", id, command);
        Article article = articleRepository.getReferenceById(id);

        Document existingContent = Jsoup.parse(article.getContent());
        Elements existingImages = existingContent.select("img[src*=\"/api/attachments/\"]");

        List<String> deletedAttachmentIds = new ArrayList<>();

        Document newContent = Jsoup.parse(command.getContent());
        Elements remainingImages = newContent.select("img[src*=\"/api/attachments/\"]");

        // src는 "/"로 스플릿하여 마지막 요소를 uuid4 패턴의 문자열이다.
        for (Element existingImage : existingImages) {
            String existingSrc = existingImage.attr("src");
            boolean isPresentInNewImages = remainingImages.stream()
                    .anyMatch(newImage -> newImage.attr("src").equals(existingSrc));
            if (!isPresentInNewImages) {
                deletedAttachmentIds.add(existingSrc.substring(existingSrc.lastIndexOf("/") + 1));
            }
        }

        // 수정하면서 삭제한 이미지를 삭제
        List<Attachment> attachments = attachmentRepository.findAllByIdIn(deletedAttachmentIds);
        attachmentRepository.deleteAllInBatch(attachments);
        attachments.stream()
                .map(attachment -> seesawApiProperties.getFilepath() + attachment.getPathName() + File.separator + attachment.getName())
                .forEach(fileManager::delete);

        Iterator<Element> iterator = newContent.select("img[src*=\"blob:\"]").iterator();

        for (MultipartFile multipartFile : command.getInlineImages()) {
            Attachment attachment = new Attachment();
            attachment.setReferenceId(article.getId());
            attachment.setOriginalName(multipartFile.getOriginalFilename());
            attachment.setName(UUID.randomUUID() + "_" + attachment.getOriginalName());
            attachment.setPathName(Attachment.Type.INLINE_IMAGE.getPath());
            attachment.setMimeType(multipartFile.getContentType());
            attachment.setSize(multipartFile.getSize());

            fileManager.write(seesawApiProperties.getFilepath() + attachment.getPathName() + File.separator + attachment.getName(), multipartFile.getBytes());
            attachmentRepository.save(attachment);

            String url = "/api/attachments/" + attachment.getId();

            // images의 src를 첨부파일을 생성 후 "/api/attachments/{id}"로 치환한다.
            if (iterator.hasNext()) {
                Element element = iterator.next();
                element.attr("src", url);
            }
        }

        Safelist safelist = Safelist.relaxed().preserveRelativeLinks(true);
        command.setContent(Jsoup.clean(newContent.body().html(), "http://localhost", safelist));

        Category category = new Category();
        category.setId(command.getCategoryId());
        article.setCategory(category);
        article.setType(command.getType());
        article.setFixed(command.isFixed());
        article.setFixedOrder(command.getFixedOrder());
        article.setTitle(command.getTitle());
        article.setContent(command.getContent());

        // 첨부파일
        for (MultipartFile multipartFile : command.getMultipartFiles()) {
            Attachment attachment = new Attachment();
            attachment.setReferenceId(article.getId());
            attachment.setOriginalName(multipartFile.getOriginalFilename());
            attachment.setName(UUID.randomUUID() + "_" + attachment.getOriginalName());
            attachment.setPathName(Attachment.Type.ATTACHMENT.getPath());
            attachment.setMimeType(multipartFile.getContentType());
            attachment.setSize(multipartFile.getSize());

            fileManager.write(seesawApiProperties.getFilepath() + attachment.getPathName() + File.separator + attachment.getName(), multipartFile.getBytes());
            attachmentRepository.save(attachment);
        }

        return new ArticleResponse(articleRepository.save(article));
    }

    @Override
    public ArticleResponse move(String id, MoveArticleRequest command) {
        logger.info("게시글 이동: id={}, command={}", id, command);
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 게시글이 존재하지 않습니다."));

        Category targetCategory = categoryRepository.findById(command.categoryId())
                .orElseThrow(() -> new NoSuchElementException("이동할 카테고리가 존재하지 않습니다."));

        if (article.getCategory().getType() != targetCategory.getType()) {
            throw new IllegalArgumentException("동일한 타입의 카테고리로만 이동할 수 있습니다.");
        }

        article.setCategory(targetCategory);

        Article savedArticle = articleRepository.save(article);
        return new ArticleResponse(savedArticle);
    }

    @Override
    public void delete(String id) {
        logger.info("게시글 삭제: id={}", id);
        Article article = articleRepository.getReferenceById(id);
        List<Reply> replies = replyRepository.findAllByArticleIdIn(Collections.singletonList(id));
        replyRepository.deleteAllInBatch(replies);
        List<View> views = viewRepository.findAllByArticleIdIn(Collections.singletonList(id));
        viewRepository.deleteAllInBatch(views);
        List<Attachment> attachments = attachmentRepository.findAllByReferenceIdIn(Collections.singletonList(id));
        attachmentRepository.deleteAllInBatch(attachments);
        attachments.stream()
                .map(attachment -> seesawApiProperties.getFilepath() + attachment.getPathName() + File.separator + attachment.getName())
                .forEach(fileManager::delete);

        articleRepository.delete(article);
    }

    @Override
    public void deleteAll(List<String> ids) {
        logger.info("게시글 일괄 삭제: ids={}", ids);
        ids.forEach(this::delete);
    }

    @Override
    public boolean isOwner(String id, String username) {
        logger.info("게시글 소유자 확인: id={}, username={}", id, username);
        ArticleResponse article = find(id);
        return article.getCreatedBy().equals(username);
    }

    @Override
    public List<ArticleResponse> getFixedArticles(String categoryId, boolean fixed, Sort sort) {
        logger.debug("고정 게시글 조회: categoryId={}, fixed={}, sort={}", categoryId, fixed, sort);
        return articleRepository.findAllByCategoryIdAndFixed(categoryId, fixed, sort)
                .stream()
                .map(ArticleResponse::new)
                .toList();
    }

    @Nullable
    @Override
    public ArticleResponse getPreviousArticle(SearchArticlesRequest search, LocalDateTime createdDate) {
        logger.debug("이전 게시글 조회: createdDate={}, search={}", createdDate, search);
        return articleQueryRepository.findFirstNext(
                search.getCategoryId(),
                search.getKeyField(),
                search.getKeyWord(),
                createdDate,
                Sort.Direction.DESC.name()
        ).map(ArticleResponse::new).orElse(null);
    }

    @Nullable
    @Override
    public ArticleResponse getNextArticle(SearchArticlesRequest search, LocalDateTime createdDate) {
        logger.debug("다음 게시글 조회: createdDate={}, search={}", createdDate, search);
        return articleQueryRepository.findFirstNext(
                search.getCategoryId(),
                search.getKeyField(),
                search.getKeyWord(),
                createdDate,
                Sort.Direction.DESC.name()
        ).map(ArticleResponse::new).orElse(null);
    }

    @Override
    public List<ReplyResponse> getReplies(String articleId) {
        return replyRepository.findAllByArticleIdIn(Collections.singletonList(articleId))
                .stream()
                .sorted(Comparator.comparing(BaseEntity::getCreatedDate))
                .map(ReplyResponse::new)
                .toList();
    }

    @Override
    public List<AttachmentResponse> getAttachments(String articleId) {
        Article article = articleRepository.getReferenceById(articleId);
        List<Attachment> attachments = attachmentRepository.findAllByReferenceIdIn(Collections.singletonList(article.getId()));
        return attachments.stream().map(AttachmentResponse::new).toList();
    }

}
