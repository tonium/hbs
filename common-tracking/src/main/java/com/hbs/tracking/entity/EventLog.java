package com.hbs.tracking.entity;

import com.hbs.tracking.enums.TrackingEventType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "event_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private UUID eventId;

    @Column(nullable = false, length = 64)
    private String traceId;

    @Column(length = 32)
    private String spanId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private TrackingEventType eventType;

    @Column(nullable = false, length = 64)
    private String serviceName;

    @Column(length = 64)
    private String orgId;

    @Column(length = 32)
    private String actorType;

    @Column(length = 128)
    private String actorId;

    @Column(length = 32)
    private String targetType;

    @Column(length = 128)
    private String targetId;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(columnDefinition = "jsonb")
    private String payload;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime occurredAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Builder
    public EventLog(String traceId, String spanId, TrackingEventType eventType,
                    String serviceName, String orgId, String actorType, String actorId,
                    String targetType, String targetId, String status, String payload) {
        this.eventId = UUID.randomUUID();
        this.traceId = traceId;
        this.spanId = spanId;
        this.eventType = eventType;
        this.serviceName = serviceName;
        this.orgId = orgId;
        this.actorType = actorType;
        this.actorId = actorId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.status = status;
        this.payload = payload;
    }
}
