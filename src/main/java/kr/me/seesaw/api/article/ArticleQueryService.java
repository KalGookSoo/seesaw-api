package kr.me.seesaw.api.article;

import kr.me.seesaw.api.attachment.dto.AttachmentResponse;
import kr.me.seesaw.api.reply.dto.ReplyResponse;

import java.util.List;

public interface ArticleQueryService {

    List<ReplyResponse> getReplies(String articleId);

    List<AttachmentResponse> getAttachments(String articleId);

}
