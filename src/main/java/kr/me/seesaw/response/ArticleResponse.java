package kr.me.seesaw.response;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import kr.me.seesaw.core.hierarchy.Hierarchical;
import kr.me.seesaw.domain.Article;
import kr.me.seesaw.domain.vo.ArticleType;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.jsoup.Jsoup;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Schema(name = "ArticleResponse", description = "게시글 모델")
@ToString(exclude = {"attachments", "replies", "views", "votes"})
@EqualsAndHashCode(exclude = {"attachments", "replies", "views", "votes"}, callSuper = true)
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class ArticleResponse extends AbstractHierarchicalResponse<ArticleResponse> implements Hierarchical<ArticleResponse, String> {

    @Comment("노출여부")
    private boolean exposed;

    @Comment("고정여부")
    private boolean fixed;

    @Comment("고정순서")
    private Integer fixedOrder;

    @Comment("제목")
    private String title;

    @Comment("본문")
    @Column(columnDefinition = "TEXT")
    @Setter(AccessLevel.PUBLIC)
    private String content;

    @Enumerated(EnumType.STRING)
    @Comment("타입")
    private ArticleType type;

    @Comment("카테고리 식별자")
    @Column(length = 36)
    private String categoryId;

    @JsonManagedReference
    private List<AttachmentResponse> attachments = new ArrayList<>();

    @JsonManagedReference
    private List<ReplyResponse> replies = new ArrayList<>();

    @JsonManagedReference
    private List<ViewResponse> views = new ArrayList<>();

    @JsonManagedReference
    private List<VoteResponse> votes = new ArrayList<>();

    @Override
    public void addChild(ArticleResponse child) {
        getChildren().add(child);
        child.setParentId(getId());
        // ArticleResponse은 SiteResponse이 아니므로 parent 참조는 설정하지 않습니다.
    }

    public ArticleResponse(Article article) {
        setBaseModel(article);
        setParentId(article.getParentId());
        this.exposed = article.isExposed();
        this.fixed = article.isFixed();
        this.fixedOrder = article.getFixedOrder();
        this.title = article.getTitle();
        this.content = article.getContent();
        this.type = article.getType();
        this.categoryId = article.getCategory() != null ? article.getCategory().getId() : null;
    }

    public String getMaskedAuthor() {
        String createdBy = getCreatedBy();
        int visibleChars = Math.min(createdBy.length(), 4);
        return createdBy.substring(0, visibleChars) + "****";
    }

    public void joinReplies(List<ReplyResponse> replies) {
        replies.stream().filter(this::isReplyForArticle).forEach(this::addReply);
    }

    public boolean isReplyForArticle(ReplyResponse reply) {
        return getId().equals(reply.getArticleId());
    }

    public void addReply(ReplyResponse reply) {
        this.replies.add(reply);
        reply.setArticle(this);
    }

    public void joinViews(List<ViewResponse> views) {
        views.stream().filter(this::isViewForArticle).forEach(this::addView);
    }

    public boolean isViewForArticle(ViewResponse view) {
        return getId().equals(view.getArticleId());
    }

    public void addView(ViewResponse view) {
        this.views.add(view);
        view.setArticle(this);
    }

    public String getPlainContent() {
        return Jsoup.parse(content).text();
    }

    public void joinAttachments(List<AttachmentResponse> attachments) {
        attachments.stream().filter(this::isAttachmentForArticle).forEach(this::addAttachment);
    }

    private boolean isAttachmentForArticle(AttachmentResponse attachment) {
        return getId().equals(attachment.getReferenceId());
    }

    private void addAttachment(AttachmentResponse attachment) {
        this.attachments.add(attachment);
    }

    public List<AttachmentResponse> getInlineImages() {
        return attachments.stream().filter(AttachmentResponse::isInlineImage).toList();
    }

    public List<AttachmentResponse> getAttachments() {
        return attachments.stream().filter(AttachmentResponse::isAttachment).toList();
    }

    public boolean isRecentlyGenerated() {
        LocalDateTime now = LocalDateTime.now();
        return !getCreatedDate().isBefore(now.minusDays(7));
    }

    public String getUrl() {
        return String.format("/articles/%s?categoryId=%s", getId(), categoryId);
    }

}
