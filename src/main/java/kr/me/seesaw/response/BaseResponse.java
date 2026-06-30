package kr.me.seesaw.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.me.seesaw.domain.BaseEntity;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@ToString
@EqualsAndHashCode
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor
public abstract class BaseResponse implements Serializable {

    @Schema(description = "식별자(UUID)", example = "8f14e45f-ea9d-4b1c-a3a4-12c4b2a9c001")
    private String id;

    @Schema(description = "생성자 계정명", example = "admin")
    private String createdBy;

    @Schema(description = "생성 IP", example = "192.168.0.10")
    private String createdIp;

    @Schema(description = "생성 일시", example = "2025-01-01T09:00:00")
    private LocalDateTime createdDate;

    @Schema(description = "최종 수정자 계정명", example = "editor")
    private String lastModifiedBy;

    @Schema(description = "최종 수정 IP", example = "192.168.0.11")
    private String lastModifiedIp;

    @Schema(description = "최종 수정 일시", example = "2025-01-02T10:30:00")
    private LocalDateTime lastModifiedDate;

    protected void setBaseModel(BaseEntity baseEntity) {
        this.id = baseEntity.getId();
        this.createdBy = baseEntity.getCreatedBy();
        this.createdIp = baseEntity.getCreatedIp();
        this.createdDate = baseEntity.getCreatedDate();
        this.lastModifiedBy = baseEntity.getLastModifiedBy();
        this.lastModifiedIp = baseEntity.getLastModifiedIp();
        this.lastModifiedDate = baseEntity.getLastModifiedDate();
    }

}
