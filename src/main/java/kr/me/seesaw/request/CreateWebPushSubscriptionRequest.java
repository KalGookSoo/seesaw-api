package kr.me.seesaw.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "웹 푸시 구독 생성 커맨드")
@Data
public class CreateWebPushSubscriptionRequest implements Serializable {

    @Schema(description = "사이트 식별자", example = "사이트 식별자")
    @NotBlank
    private String siteId;

    @Schema(description = "PushSubscription endpoint", example = "https://fcm.googleapis.com/fcm/send/...")
    @NotBlank
    private String endpoint;

    @Schema(description = "PushSubscription keys")
    @Valid
    @NotNull
    private Keys keys;

    @Data
    public static class Keys implements Serializable {

        @Schema(description = "PushSubscription keys.p256dh")
        @NotBlank
        private String p256dh;

        @Schema(description = "PushSubscription keys.auth")
        @NotBlank
        private String auth;

    }

}
