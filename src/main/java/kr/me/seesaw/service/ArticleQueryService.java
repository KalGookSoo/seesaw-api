package kr.me.seesaw.service;

import kr.me.seesaw.response.ArticleResponse;
import kr.me.seesaw.response.AttachmentResponse;
import kr.me.seesaw.response.ReplyResponse;

import java.util.List;

public interface ArticleQueryService {

    ArticleResponse getArticleAggregation(String id);

    List<ReplyResponse> getReplies(String articleId);

    List<AttachmentResponse> getAttachments(String articleId);

}
