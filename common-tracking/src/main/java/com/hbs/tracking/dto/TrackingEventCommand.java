package com.hbs.tracking.dto;

import com.hbs.tracking.enums.TrackingEventType;

public record TrackingEventCommand(
        String traceId,
        String spanId,
        TrackingEventType eventType,
        String serviceName,
        String orgId,
        String actorType,
        String actorId,
        String targetType,
        String targetId,
        String status,
        String payload
) {
    public static TrackingEventCommand of(String traceId, TrackingEventType eventType,
                                          String serviceName, String orgId,
                                          String targetType, String targetId,
                                          String status) {
        return new TrackingEventCommand(
                traceId, null, eventType, serviceName,
                orgId, null, null, targetType, targetId, status, null
        );
    }
}
