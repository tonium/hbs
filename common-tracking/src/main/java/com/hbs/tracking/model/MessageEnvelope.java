package com.hbs.tracking.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Kafka 및 SSE 게이트웨이에서 공통으로 사용하는 메시지 외형(Envelope).
 * ingestion-service → Kafka → sse-gateway 흐름에서 직렬화/역직렬화에 사용된다.
 *
 * <pre>
 * 키: user-messages topic, key = userId
 * </pre>
 */
public record MessageEnvelope(
        String messageId,
        String orgId,
        String userId,
        String programId,
        String channelId,
        String type,
        int schemaVersion,
        Instant ts,
        int ttlSeconds,
        String traceId,
        Map<String, Object> payload
) {
    public static MessageEnvelope create(String orgId, String userId, String programId,
                                          String channelId, String type, int ttlSeconds,
                                          String traceId, Map<String, Object> payload) {
        return new MessageEnvelope(
                UUID.randomUUID().toString(),
                orgId, userId, programId, channelId, type,
                1, Instant.now(), ttlSeconds, traceId, payload
        );
    }
}
