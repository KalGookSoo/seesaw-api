package kr.me.seesaw.service;

import kr.me.seesaw.response.AttachmentResponse;

import java.util.List;

public interface AttachmentQueryService {

    List<AttachmentResponse> getAttachments(String referenceId);

}
