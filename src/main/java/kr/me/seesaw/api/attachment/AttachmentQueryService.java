package kr.me.seesaw.api.attachment;

import kr.me.seesaw.api.attachment.dto.AttachmentResponse;

import java.util.List;

public interface AttachmentQueryService {

    List<AttachmentResponse> getAttachments(String referenceId);

    List<AttachmentResponse> getArticleAttachments(String articleId);

}
