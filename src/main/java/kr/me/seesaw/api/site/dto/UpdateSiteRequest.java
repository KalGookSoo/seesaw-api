package kr.me.seesaw.api.site.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.me.seesaw.core.domain.user.Address;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Schema(description = "사이트 수정 커맨드")
@Data
public class UpdateSiteRequest implements Serializable {

    private static final long MAX_FILE_SIZE = 50L * 1024L * 1024L;

    @Parameter(description = "이름")
    @Schema(description = "이름", example = "이름")
    @NotBlank
    @NotNull
    private String name;

    @Parameter(description = "도메인이름")
    @Schema(description = "도메인이름", example = "www.example.com")
    @NotBlank
    @NotNull
    private String domainName;

    @Parameter(description = "설명")
    @Schema(description = "설명", example = "설명")
    @NotBlank
    @NotNull
    private String description;

    @Parameter(description = "분류코드")
    @Schema(description = "분류코드", example = "분류코드")
    @NotBlank
    @NotNull
    private String distributionCode;

    @Parameter(description = "검색 엔진 노출 여부")
    @Schema(description = "검색 엔진 노출 여부", example = "true")
    private boolean searchEngineExposed;

    @Parameter(description = "이미지 노출 여부")
    @Schema(description = "이미지 노출 여부", example = "true")
    private boolean imageExposed;

    @Parameter(description = "테마 색상")
    @Schema(description = "테마 색상", example = "#cc4202")
    private String themeColor;

    @Parameter(description = "배경 색상")
    @Schema(description = "배경 색상", example = "#ffffff")
    private String backgroundColor;

    @Parameter(description = "태그")
    @Schema(description = "태그", example = "태그1,태그2")
    @NotBlank
    @NotNull
    private String tags;

    @Parameter(description = "주소")
    @Schema(description = "주소")
    @NotNull
    private Address address = Address.empty();

    @Parameter(description = "연락처")
    @Schema(description = "연락처", example = "010-1234-5678")
    @NotBlank
    @NotNull
    private String contactNumber;

    @Parameter(description = "소개글")
    @Schema(description = "소개글", example = "독서모임 홈페이지")
    @NotBlank
    @NotNull
    private String intro;

    @Parameter(description = "본문")
    @Schema(description = "본문", example = "독서 모임을 시작으로 대전시에서 주최하는 다양한 문화 및 지원 행사에 참여하고 싶으신 분들 환영합니다!")
    @NotBlank
    @NotNull
    private String content;

    @Parameter(description = "프로필 이미지")
    @Schema(description = "프로필 이미지")
    private MultipartFile profileImage;

    @AssertTrue(message = "프로필 이미지는 최대 50MB까지 업로드할 수 있습니다.")
    public boolean isProfileImageSizeValid() {
        return profileImage == null || profileImage.isEmpty() || profileImage.getSize() <= MAX_FILE_SIZE;
    }

}
