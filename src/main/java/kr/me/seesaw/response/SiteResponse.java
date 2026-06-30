package kr.me.seesaw.response;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.me.seesaw.core.hierarchy.Hierarchical;
import kr.me.seesaw.domain.Attachment;
import kr.me.seesaw.domain.Category;
import kr.me.seesaw.domain.Site;
import kr.me.seesaw.domain.vo.Address;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Schema(name = "SiteResponse", description = "사이트 모델")
@ToString(exclude = {"categories", "attachments"})
@EqualsAndHashCode(exclude = {"categories", "attachments"}, callSuper = true)
@Getter
@AllArgsConstructor
public final class SiteResponse extends AbstractHierarchicalResponse<SiteResponse> implements Hierarchical<SiteResponse, String> {

    @Schema(description = "이름")
    private final String name;

    @Schema(description = "도메인이름")
    private final String domainName;

    @Schema(description = "설명")
    private final String description;

    @Schema(description = "분류코드")
    private final String distributionCode;

    @Schema(description = "검색엔진 노출여부")
    private final boolean searchEngineExposed;

    @Schema(description = "이미지 노출여부")
    private final boolean imageExposed;

    @Schema(description = "테마 색상", example = "#cc4202")
    private final String themeColor;

    @Schema(description = "배경 색상", example = "#ffffff")
    private final String backgroundColor;

    @Schema(description = "태그")
    private final String tags;

    @Schema(description = "주소")
    private final Address address;

    @Schema(description = "연락처")
    private final String contactNumber;

    @Schema(description = "소개글")
    private final String intro;

    @Schema(description = "본문")
    private final String content;

    @JsonManagedReference
    private final List<CategoryResponse> categories = new ArrayList<>();

    @JsonManagedReference
    private final List<AttachmentResponse> attachments = new ArrayList<>();

    @Override
    public void addChild(SiteResponse child) {
        getChildren().add(child);
        child.setParentId(getId());
        child.setParent(this);
    }

    public SiteResponse(Site site) {
        setBaseModel(site);
        setParentId(site.getParentId());
        name = site.getName();
        domainName = site.getDomainName();
        description = site.getDescription();
        distributionCode = site.getDistributionCode();
        searchEngineExposed = site.isSearchEngineExposed();
        imageExposed = site.isImageExposed();
        themeColor = site.getThemeColor().toString();
        backgroundColor = site.getBackgroundColor().toString();
        tags = site.getTags();
        address = site.getAddress();
        contactNumber = site.getContactNumber();
        intro = site.getIntro();
        content = site.getContent();
    }

    public AttachmentResponse getProfileImage() {
        return attachments.stream()
                .filter(attachment -> Attachment.Type.PROFILE.getPath().equals(attachment.getPathName()))
                .findFirst()
                .orElse(null);
    }

    public AttachmentResponse getBackgroundImage() {
        return attachments.stream()
                .filter(attachment -> Attachment.Type.BACKGROUND_IMAGE.getPath().equals(attachment.getPathName()))
                .findFirst()
                .orElse(null);
    }

    public void addAttachment(AttachmentResponse attachment) {
        attachments.add(attachment);
    }

    public void addCategory(CategoryResponse category) {
        categories.add(category);
    }

}
