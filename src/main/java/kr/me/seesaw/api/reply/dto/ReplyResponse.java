package kr.me.seesaw.api.reply.dto;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.me.seesaw.api.attachment.dto.AttachmentResponse;
import kr.me.seesaw.api.vote.dto.VoteResponse;
import kr.me.seesaw.core.support.dto.AbstractHierarchicalResponse;
import kr.me.seesaw.core.domain.reply.Reply;
import kr.me.seesaw.core.support.hierarchy.Hierarchical;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;

@Schema(name = "ReplyResponse", description = "댓글 모델")
@ToString(exclude = {"attachments", "votes"})
@EqualsAndHashCode(exclude = {"attachments", "votes"}, callSuper = true)
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class ReplyResponse extends AbstractHierarchicalResponse<ReplyResponse> implements Hierarchical<ReplyResponse, String> {

    @Comment("노출여부")
    private boolean exposed;

    @Comment("본문")
    @Schema(description = "댓글 본문")
    private String content;

    @Comment("게시글 식별자")
    @Schema(description = "게시글 식별자(UUID)")
    private String articleId;

    @JsonManagedReference
    private List<AttachmentResponse> attachments = new ArrayList<>();

    @JsonManagedReference
    private List<VoteResponse> votes = new ArrayList<>();

    public ReplyResponse(Reply reply) {
        setBaseModel(reply);
        setParentId(reply.getParentId());
        this.exposed = reply.isExposed();
        this.content = reply.getContent();
        this.articleId = (reply.getArticle() != null ? reply.getArticle().getId() : null);
    }

    @Override
    public void addChild(@NonNull ReplyResponse child) {
        getChildren().add(child);
        child.setParentId(getId());
        child.setParent(this);
    }

    public String getMaskedAuthor() {
        String createdBy = getCreatedBy();
        int visibleChars = Math.min(createdBy.length(), 4);
        return createdBy.substring(0, visibleChars) + "****";
    }

    public void addAttachment(AttachmentResponse attachment) {
        attachments.add(attachment);
    }

    public void joinAttachments(List<AttachmentResponse> attachments) {
        attachments.stream().filter(this::isAttachmentForReply).forEach(this::addAttachment);
    }

    private boolean isAttachmentForReply(AttachmentResponse attachment) {
        return getId().equals(attachment.getReferenceId());
    }

    public List<AttachmentResponse> getInlineImages() {
        return attachments.stream().filter(AttachmentResponse::isInlineImage).toList();
    }

    public List<AttachmentResponse> getAttachments() {
        return attachments.stream().filter(AttachmentResponse::isAttachment).toList();
    }

}
