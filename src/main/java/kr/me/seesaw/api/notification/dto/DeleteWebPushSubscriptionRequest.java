package kr.me.seesaw.api.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "웹 푸시 구독 삭제 커맨드")
@Data
public class DeleteWebPushSubscriptionRequest implements Serializable {

    @Schema(description = "사이트 식별자", example = "사이트 식별자")
    @NotBlank
    private String siteId;

    @Schema(description = "PushSubscription endpoint", example = "https://fcm.googleapis.com/fcm/send/...")
    @NotBlank
    private String endpoint;

}
