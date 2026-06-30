package kr.me.seesaw.service;

import kr.me.seesaw.request.CreateReplyRequest;
import kr.me.seesaw.request.UpdateReplyRequest;
import kr.me.seesaw.response.ReplyResponse;

public interface ReplyService {

    ReplyResponse find(String id);

    ReplyResponse create(CreateReplyRequest command);

    ReplyResponse update(String id, UpdateReplyRequest command);

    void delete(String id);

}
