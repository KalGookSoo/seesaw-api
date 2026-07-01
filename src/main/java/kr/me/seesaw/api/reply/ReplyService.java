package kr.me.seesaw.api.reply;

import kr.me.seesaw.api.reply.dto.CreateReplyRequest;
import kr.me.seesaw.api.reply.dto.UpdateReplyRequest;
import kr.me.seesaw.api.reply.dto.ReplyResponse;

public interface ReplyService {

    ReplyResponse find(String id);

    ReplyResponse create(CreateReplyRequest command);

    ReplyResponse update(String id, UpdateReplyRequest command);

    void delete(String id);

}
