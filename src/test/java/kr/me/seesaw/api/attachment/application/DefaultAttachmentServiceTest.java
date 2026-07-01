package kr.me.seesaw.api.attachment.application;

import kr.me.seesaw.api.attachment.AttachmentService;
import kr.me.seesaw.api.attachment.dto.AttachmentResponse;
import kr.me.seesaw.api.attachment.dto.CreateAttachmentRequest;
import kr.me.seesaw.core.domain.attachment.Attachment;
import kr.me.seesaw.core.domain.attachment.persistence.AttachmentRepositoryImpl;
import kr.me.seesaw.core.support.file.LocalFileManager;
import kr.me.seesaw.framework.context.properties.SeesawApiProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.UUID;

@ActiveProfiles({"test"})
@DataJpaTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Import({AttachmentRepositoryImpl.class, DefaultAttachmentService.class, LocalFileManager.class, SeesawApiProperties.class})
class DefaultAttachmentServiceTest {

    private final TestEntityManager entityManager;

    private final AttachmentService attachmentService;

    private final SeesawApiProperties properties;

    @TempDir
    private Path tempDir;

    public DefaultAttachmentServiceTest(
            TestEntityManager entityManager,
            AttachmentService attachmentService,
            SeesawApiProperties properties
    ) {
        this.entityManager = entityManager;
        this.attachmentService = attachmentService;
        this.properties = properties;
    }

    @BeforeEach
    void setUp() {
        properties.setFilepath(tempDir + "/");
    }

    @Test
    @DisplayName("첨부파일 생성 시 첨부파일 모델을 반환합니다.")
    void createAttachmentShouldReturnAttachmentModel() {
        // given
        String referenceId = UUID.randomUUID().toString();
        Attachment.Type type = Attachment.Type.ATTACHMENT;
        MultipartFile multipartFile = new MockMultipartFile("profileImage", "profile-image.jpeg", MediaType.IMAGE_JPEG_VALUE, new byte[4]);
        CreateAttachmentRequest createAttachmentCommand = new CreateAttachmentRequest(referenceId, type, multipartFile);

        // when
        AttachmentResponse attachmentModel = attachmentService.createAttachment(createAttachmentCommand);
        entityManager.flush();

        // then
        Assertions.assertEquals(referenceId, attachmentModel.getReferenceId());
    }

    @Test
    @DisplayName("첨부파일 상세 조회 시 파일이 존재할 경우 첨부파일 모델을 반환합니다.")
    void getAttachmentByIdShouldReturnAttachmentModel() {
        // given
        String referenceId = UUID.randomUUID().toString();
        Attachment.Type type = Attachment.Type.ATTACHMENT;
        MultipartFile multipartFile = new MockMultipartFile("profileImage", "profile-image.jpeg", MediaType.IMAGE_JPEG_VALUE, new byte[4]);
        CreateAttachmentRequest createAttachmentCommand = new CreateAttachmentRequest(referenceId, type, multipartFile);
        String id = attachmentService.createAttachment(createAttachmentCommand).getId();
        entityManager.flush();
        entityManager.clear();

        // when
        AttachmentResponse attachmentModel = attachmentService.getAttachmentById(id);

        // then
        Assertions.assertEquals(id, attachmentModel.getId());
    }

    @Test
    @DisplayName("첨부파일 상세 조회 시 파일을 찾을 수 없는 경우 NoSuchElementException을 반환합니다.")
    void getAttachmentByIdShouldThrowNoSuchElementException() {
        // given
        String id = "test-attachment-id";

        // when
        Exception exception = Assertions.assertThrows(NoSuchElementException.class, () -> attachmentService.getAttachmentById(id));

        // then
        Assertions.assertEquals(NoSuchElementException.class, exception.getClass());
    }

}
