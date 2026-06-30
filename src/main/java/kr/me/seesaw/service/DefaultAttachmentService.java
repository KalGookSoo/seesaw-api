package kr.me.seesaw.service;

import kr.me.seesaw.request.CreateAttachmentRequest;
import kr.me.seesaw.api.framework.config.SeesawProperties;
import kr.me.seesaw.core.file.FileManager;
import kr.me.seesaw.domain.Attachment;
import kr.me.seesaw.response.AttachmentResponse;
import kr.me.seesaw.repository.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

@Transactional
@Service
@RequiredArgsConstructor
public class DefaultAttachmentService implements AttachmentService, AttachmentQueryService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final SeesawProperties seesawProperties;

    private final AttachmentRepository attachmentRepository;

    private final FileManager fileManager;

    /**
     * 요청된 바이너리 데이터를 파일로 쓰고 데이터베이스 모델로 영속화 후 첨부파일 모델로 변환하여 반환합니다.
     * 입출력 예외 시 IllegalArgumentException으로 wrapping하여 던집니다.
     *
     * @param command 첨부파일 생성 커맨드
     * @return 첨부파일 모델
     * @throws IllegalArgumentException 예외
     */
    @Override
    public AttachmentResponse createAttachment(CreateAttachmentRequest command) throws IllegalArgumentException {
        logger.info("첨부파일 생성: command={}", command);
        if (command.getMultipartFile() == null || command.getMultipartFile().isEmpty()) {
            throw new IllegalArgumentException("첨부파일이 존재하지 않습니다.");
        }
        Attachment attachment = new Attachment();
        attachment.setReferenceId(command.getReferenceId());
        attachment.setPathName(command.getType().getPath());
        attachment.setMimeType(command.getMultipartFile().getContentType());
        attachment.setSize(command.getMultipartFile().getSize());
        attachment.setOriginalName(command.getMultipartFile().getOriginalFilename());
        attachment.setName(java.util.UUID.randomUUID() + "_" + attachment.getOriginalName());

        byte[] bytes;
        try {
            bytes = command.getMultipartFile().getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        writeFile(seesawProperties.getFilepath() + attachment.getPathName() + File.separator + attachment.getName(), bytes);
        attachmentRepository.save(attachment);
        return new AttachmentResponse(attachment);
    }

    @Transactional(readOnly = true)
    @Override
    public AttachmentResponse getAttachmentById(String id) {
        logger.debug("첨부파일 조회: id={}", id);
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 첨부파일: " + id));
        return new AttachmentResponse(attachment);
    }

    @Override
    public String getAbsolutePath(String pathname, String name) {
        logger.debug("절대 경로 조회: pathname={}, name={}", pathname, name);
        return seesawProperties.getFilepath() + pathname + File.separator + name;
    }

    @Override
    public void deleteAttachment(String id) {
        logger.info("첨부파일 삭제: id={}", id);
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 첨부파일: " + id));

        fileManager.delete(seesawProperties.getFilepath() + attachment.getPathName() + File.separator + attachment.getName());
        attachmentRepository.delete(attachment);
    }

    private void writeFile(String pathname, byte[] bytes) {
        logger.info("파일 쓰기: pathname={}", pathname);
        try {
            fileManager.write(pathname, bytes);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public List<AttachmentResponse> getAttachments(String referenceId) {
        logger.debug("첨부파일 조회: referenceId={}", referenceId);
        return attachmentRepository.findAllByReferenceIdIn(Collections.singletonList(referenceId))
                .stream()
                .map(AttachmentResponse::new)
                .toList();
    }

}
