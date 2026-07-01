package kr.me.seesaw.api.attachment;

import kr.me.seesaw.api.attachment.dto.CreateAttachmentRequest;
import kr.me.seesaw.api.attachment.dto.AttachmentResponse;

public interface AttachmentService {

    AttachmentResponse createAttachment(CreateAttachmentRequest command);

    AttachmentResponse getAttachmentById(String id);

    String getAbsolutePath(String pathname, String name);

    void deleteAttachment(String id);

}
