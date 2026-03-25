package com.hbs.tracking.dto;

import com.hbs.tracking.enums.DeliveryStage;
import com.hbs.tracking.enums.DeliveryStatus;

import java.util.UUID;

public record DeliveryEventCommand(
        UUID messageId,
        String traceId,
        UUID jobId,
        String orgId,
        String userId,
        String programId,
        String channelId,
        DeliveryStage deliveryStage,
        DeliveryStatus deliveryStatus,
        String errorCode,
        String errorMessage
) {
    public static DeliveryEventCommand success(UUID messageId, String traceId, UUID jobId,
                                               String orgId, String userId,
                                               String programId, String channelId,
                                               DeliveryStage stage) {
        return new DeliveryEventCommand(
                messageId, traceId, jobId, orgId, userId,
                programId, channelId, stage, DeliveryStatus.SUCCESS, null, null
        );
    }

    public static DeliveryEventCommand failure(UUID messageId, String traceId, UUID jobId,
                                               String orgId, String userId,
                                               String programId, String channelId,
                                               DeliveryStage stage,
                                               String errorCode, String errorMessage) {
        return new DeliveryEventCommand(
                messageId, traceId, jobId, orgId, userId,
                programId, channelId, stage, DeliveryStatus.FAILED, errorCode, errorMessage
        );
    }
}
