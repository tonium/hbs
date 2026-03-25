package com.hbs.subscription.dto.response;

import com.hbs.subscription.entity.Subscription;

import java.time.OffsetDateTime;

public record SubscribeResponse(
        Long id,
        String orgId,
        String userId,
        String programId,
        String channelId,
        String status,
        OffsetDateTime createdAt
) {
    public static SubscribeResponse from(Subscription subscription) {
        return new SubscribeResponse(
                subscription.getId(),
                subscription.getOrgId(),
                subscription.getUserId(),
                subscription.getProgramId(),
                subscription.getChannelId(),
                subscription.getStatus().name(),
                subscription.getCreatedAt()
        );
    }
}
