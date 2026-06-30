package kr.me.seesaw.service;

import kr.me.seesaw.request.CreateSiteRequest;
import kr.me.seesaw.api.framework.config.SeesawProperties;
import kr.me.seesaw.core.authentication.PrincipalProvider;
import kr.me.seesaw.core.file.FileManager;
import kr.me.seesaw.domain.Attachment;
import kr.me.seesaw.domain.RoleMapping;
import kr.me.seesaw.domain.Site;
import kr.me.seesaw.domain.User;
import kr.me.seesaw.domain.vo.SiteColor;
import kr.me.seesaw.event.SiteCreatedEvent;
import kr.me.seesaw.response.AttachmentResponse;
import kr.me.seesaw.response.SiteResponse;
import kr.me.seesaw.repository.AttachmentRepository;
import kr.me.seesaw.repository.SiteRepository;
import kr.me.seesaw.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Transactional
@Service
@RequiredArgsConstructor
public class DefaultSiteService implements SiteService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final SeesawProperties seesawProperties;

    private final SiteRepository siteRepository;

    private final AttachmentRepository attachmentRepository;

    private final UserRepository userRepository;

    private final FileManager fileManager;

    private final PrincipalProvider principalProvider;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    @Override
    public SiteResponse getSiteById(String id) {
        logger.debug("사이트 상세 조회: id={}", id);
        return siteRepository.findById(id)
                .map(SiteResponse::new)
                .orElseThrow(() -> new NoSuchElementException("사이트를 찾을 수 없습니다. id: " + id));
    }

    @Transactional(readOnly = true)
    @Override
    public SiteResponse getSiteByDomainName(String domainName) {
        logger.debug("도메인명으로 사이트 조회: domainName={}", domainName);
        return siteRepository.findByDomainName(domainName)
                .map(SiteResponse::new)
                .orElseThrow(() -> new NoSuchElementException("사이트를 찾을 수 없습니다. domainName: " + domainName));
    }

    @Transactional(readOnly = true)
    @Override
    public List<SiteResponse> getOwnSites(String username) {
        logger.debug("소유 사이트 목록 조회: username={}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("계정을 찾을 수 없습니다. username: " + username));

        logger.debug("계정 식별자로 roleMappingRepository.findAllByUserId로 계정에 매핑된 siteId 목록 추출");
        List<String> siteIds = user.getRoleMappings()
                .stream()
                .map(RoleMapping::getSite)
                .map(Site::getId)
                .toList();

        return siteRepository.findAllByIdIn(siteIds)
                .stream()
                .map(SiteResponse::new)
                .toList();
    }

    @Override
    public SiteResponse createSite(CreateSiteRequest command) throws IOException {
        logger.info("사이트 생성: command={}", command);
        Site site = new Site();
        site.setName(command.getName());
        site.setDomainName(command.getDomainName());
        site.setDescription(command.getDescription());
        site.setDistributionCode(command.getDistributionCode());
        site.setSearchEngineExposed(command.isSearchEngineExposed());
        site.setImageExposed(command.isImageExposed());
        site.setThemeColor(SiteColor.themeOrDefault(command.getThemeColor()));
        site.setBackgroundColor(SiteColor.backgroundOrDefault(command.getBackgroundColor()));
        site.setTags(command.getTags());
        site.setAddress(command.getAddress());
        site.setContactNumber(command.getContactNumber());
        site.setIntro(command.getIntro());
        site.setContent(command.getContent());

        Site newSite = siteRepository.insert(site);

        Authentication authentication = principalProvider.getAuthentication();
        eventPublisher.publishEvent(new SiteCreatedEvent(newSite.getId(), authentication.getName()));

        SiteResponse siteModel = new SiteResponse(newSite);

        // 프로필 이미지
        if (command.hasProfileImage()) {
            // 쓰기
            Attachment attachment = new Attachment();
            attachment.setReferenceId(newSite.getId());
            attachment.setOriginalName(command.getProfileImage().getOriginalFilename());
            attachment.setName(UUID.randomUUID() + "_" + attachment.getOriginalName());
            attachment.setPathName(Attachment.Type.INLINE_IMAGE.getPath());
            attachment.setMimeType(command.getProfileImage().getContentType());
            attachment.setSize(command.getProfileImage().getSize());

            writeFile(seesawProperties.getFilepath() + attachment.getPathName() + File.separator + attachment.getName(), command.getProfileImage().getBytes());

            // 영속화
            attachmentRepository.save(attachment);
            siteModel.addAttachment(new AttachmentResponse(attachment));
        }

        // 배경 이미지
        if (command.hasBackgroundImage()) {
            // 쓰기
            Attachment attachment = new Attachment();
            attachment.setReferenceId(newSite.getId());
            attachment.setOriginalName(command.getBackgroundImage().getOriginalFilename());
            attachment.setName(UUID.randomUUID() + "_" + attachment.getOriginalName());
            attachment.setPathName(Attachment.Type.BACKGROUND_IMAGE.getPath());
            attachment.setMimeType(command.getBackgroundImage().getContentType());
            attachment.setSize(command.getBackgroundImage().getSize());

            writeFile(seesawProperties.getFilepath() + attachment.getPathName() + File.separator + attachment.getName(), command.getBackgroundImage().getBytes());

            // 영속화
            attachmentRepository.save(attachment);
            siteModel.addAttachment(new AttachmentResponse(attachment));
        }
        return siteModel;
    }

    @Override
    public SiteResponse updateSite(String id, CreateSiteRequest command) throws IOException {
        logger.info("사이트 수정: id={}, command={}", id, command);
        Site site = siteRepository.getReferenceById(id);
        site.setName(command.getName());
        site.setDomainName(command.getDomainName());
        site.setDescription(command.getDescription());
        site.setDistributionCode(command.getDistributionCode());
        site.setSearchEngineExposed(command.isSearchEngineExposed());
        site.setImageExposed(command.isImageExposed());
        site.setThemeColor(SiteColor.themeOrDefault(command.getThemeColor()));
        site.setBackgroundColor(SiteColor.backgroundOrDefault(command.getBackgroundColor()));
        site.setTags(command.getTags());
        site.setAddress(command.getAddress());
        site.setContactNumber(command.getContactNumber());
        site.setIntro(command.getIntro());
        site.setContent(command.getContent());

        siteRepository.update(site);
        SiteResponse siteModel = new SiteResponse(site);

        // 기존 파일 조회
        List<Attachment> attachments = attachmentRepository.findAllByReferenceIdIn(Collections.singletonList(site.getId()));

        // 프로필 이미지
        if (command.hasProfileImage()) {
            // 기존 첨부파일 제거
            List<Attachment> profileImages = attachments.stream().filter(attachment -> Attachment.Type.PROFILE.getPath().equals(attachment.getPathName())).toList();
            attachmentRepository.deleteAllInBatch(profileImages);
            attachments.stream().map(attachment -> seesawProperties.getFilepath() + attachment.getPathName() + File.separator + attachment.getName()).forEach(fileManager::delete);

            // 쓰기
            Attachment attachment = new Attachment();
            attachment.setReferenceId(site.getId());
            attachment.setOriginalName(command.getProfileImage().getOriginalFilename());
            attachment.setName(UUID.randomUUID() + "_" + attachment.getOriginalName());
            attachment.setPathName(Attachment.Type.INLINE_IMAGE.getPath());
            attachment.setMimeType(command.getProfileImage().getContentType());
            attachment.setSize(command.getProfileImage().getSize());

            writeFile(seesawProperties.getFilepath() + attachment.getPathName() + File.separator + attachment.getName(), command.getProfileImage().getBytes());

            // 영속화
            attachmentRepository.save(attachment);
            siteModel.addAttachment(new AttachmentResponse(attachment));
        }

        // 배경 이미지
        if (command.hasBackgroundImage()) {
            // 기존 첨부파일 제거
            List<Attachment> backgroundImages = attachments.stream().filter(attachment -> Attachment.Type.BACKGROUND_IMAGE.getPath().equals(attachment.getPathName())).toList();
            attachmentRepository.deleteAllInBatch(backgroundImages);
            attachments.stream().map(attachment -> seesawProperties.getFilepath() + attachment.getPathName() + File.separator + attachment.getName()).forEach(fileManager::delete);

            // 쓰기
            Attachment attachment = new Attachment();
            attachment.setReferenceId(site.getId());
            attachment.setOriginalName(command.getBackgroundImage().getOriginalFilename());
            attachment.setName(UUID.randomUUID() + "_" + attachment.getOriginalName());
            attachment.setPathName(Attachment.Type.BACKGROUND_IMAGE.getPath());
            attachment.setMimeType(command.getBackgroundImage().getContentType());
            attachment.setSize(command.getBackgroundImage().getSize());

            writeFile(seesawProperties.getFilepath() + attachment.getPathName() + File.separator + attachment.getName(), command.getBackgroundImage().getBytes());

            // 영속화
            attachmentRepository.save(attachment);
            siteModel.addAttachment(new AttachmentResponse(attachment));
        }
        return siteModel;
    }

    @Override
    public void deleteSite(String id) {
        logger.info("사이트 삭제: id={}", id);
        // 첨부파일 삭제
        List<Attachment> attachments = attachmentRepository.findAllByReferenceIdIn(Collections.singletonList(id));
        attachmentRepository.deleteAllInBatch(attachments);
        attachments.stream().map(attachment -> seesawProperties.getFilepath() + attachment.getPathName() + File.separator + attachment.getName()).forEach(fileManager::delete);

        siteRepository.deleteById(id);
    }

    private void writeFile(String pathname, byte[] bytes) {
        logger.info("파일 쓰기: pathname={}", pathname);
        try {
            fileManager.write(pathname, bytes);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
