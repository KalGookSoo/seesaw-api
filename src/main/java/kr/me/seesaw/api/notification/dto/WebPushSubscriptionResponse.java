package kr.me.seesaw.api.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.me.seesaw.core.domain.notification.WebPushSubscription;
import kr.me.seesaw.core.support.dto.BaseResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Schema(name = "WebPushSubscriptionResponse", description = "웹 푸시 구독 모델")
@Getter
@Setter
@NoArgsConstructor
public final class WebPushSubscriptionResponse extends BaseResponse {

    @Schema(description = "사이트 식별자")
    private String siteId;

    @Schema(description = "계정 식별자")
    private String userId;

    @Schema(description = "PushSubscription endpoint")
    private String endpoint;

    @Schema(description = "활성 여부")
    private boolean enabled;

    @Schema(description = "마지막 사용 일시")
    private LocalDateTime lastUsedAt;

    public WebPushSubscriptionResponse(WebPushSubscription subscription) {
        setBaseModel(subscription);
        this.siteId = subscription.getSiteId();
        this.userId = subscription.getUserId();
        this.endpoint = subscription.getEndpoint();
        this.enabled = subscription.isEnabled();
        this.lastUsedAt = subscription.getLastUsedAt();
    }

}
