package kr.me.seesaw.request;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.me.seesaw.domain.vo.Address;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Schema(description = "사이트 생성 커맨드")
@ToString
@AllArgsConstructor
@EqualsAndHashCode
@Getter
public final class CreateSiteRequest implements Serializable {

    private static final long MAX_FILE_SIZE = 50L * 1024L * 1024L;

    @Parameter(description = "이름")
    @Schema(description = "이름", example = "이름")
    @NotBlank
    @NotNull
    private final String name;

    @Parameter(description = "도메인이름")
    @Schema(description = "도메인이름", example = "www.example.com")
    @NotBlank
    @NotNull
    private final String domainName;

    @Parameter(description = "설명")
    @Schema(description = "설명", example = "설명")
    private final String description;

    @Parameter(description = "분류코드")
    @Schema(description = "분류코드", example = "분류코드")
    private final String distributionCode;

    @Parameter(description = "검색 엔진 노출 여부")
    @Schema(description = "검색 엔진 노출 여부", example = "true")
    private final boolean searchEngineExposed;

    @Parameter(description = "이미지 노출 여부")
    @Schema(description = "이미지 노출 여부", example = "true")
    private final boolean imageExposed;

    @Parameter(description = "테마 색상")
    @Schema(description = "테마 색상", example = "#cc4202")
    private final String themeColor;

    @Parameter(description = "배경 색상")
    @Schema(description = "배경 색상", example = "#ffffff")
    private final String backgroundColor;

    @Parameter(description = "태그")
    @Schema(description = "태그", example = "태그1,태그2")
    private final String tags;

    @Parameter(description = "주소")
    @Schema(description = "주소")
    private final Address address;

    @Parameter(description = "연락처")
    @Schema(description = "연락처", example = "010-1234-5678")
    private final String contactNumber;

    @Parameter(description = "소개글")
    @Schema(description = "소개글", example = "독서모임 홈페이지")
    private final String intro;

    @Parameter(description = "본문")
    @Schema(description = "본문", example = "독서 모임을 시작으로 대전시에서 주최하는 다양한 문화 및 지원 행사에 참여하고 싶으신 분들 환영합니다!")
    private final String content;

    @Parameter(description = "프로필 이미지")
    @Schema(description = "프로필 이미지")
    private final MultipartFile profileImage;

    @Parameter(description = "배경 이미지")
    @Schema(description = "배경 이미지")
    private final MultipartFile backgroundImage;

    public boolean hasProfileImage() {
        return null != profileImage && !profileImage.isEmpty();
    }

    public boolean hasBackgroundImage() {
        return null != backgroundImage && !backgroundImage.isEmpty();
    }

    @AssertTrue(message = "프로필 이미지는 최대 50MB까지 업로드할 수 있습니다.")
    public boolean isProfileImageSizeValid() {
        return isMultipartFileSizeValid(profileImage);
    }

    @AssertTrue(message = "배경 이미지는 최대 50MB까지 업로드할 수 있습니다.")
    public boolean isBackgroundImageSizeValid() {
        return isMultipartFileSizeValid(backgroundImage);
    }

    private boolean isMultipartFileSizeValid(MultipartFile multipartFile) {
        return multipartFile == null || multipartFile.isEmpty() || multipartFile.getSize() <= MAX_FILE_SIZE;
    }

}
