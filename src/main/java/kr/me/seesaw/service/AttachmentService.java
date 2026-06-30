package kr.me.seesaw.service;

import kr.me.seesaw.request.CreateAttachmentRequest;
import kr.me.seesaw.response.AttachmentResponse;

public interface AttachmentService {

    AttachmentResponse createAttachment(CreateAttachmentRequest command);

    AttachmentResponse getAttachmentById(String id);

    String getAbsolutePath(String pathname, String name);

    void deleteAttachment(String id);

}
