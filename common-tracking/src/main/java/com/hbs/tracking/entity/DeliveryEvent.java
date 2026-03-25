package com.hbs.tracking.entity;

import com.hbs.tracking.enums.DeliveryStage;
import com.hbs.tracking.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "delivery_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID messageId;

    @Column(nullable = false, length = 64)
    private String traceId;

    private UUID jobId;

    @Column(length = 64)
    private String orgId;

    @Column(nullable = false, length = 64)
    private String userId;

    @Column(nullable = false, length = 64)
    private String programId;

    @Column(length = 64)
    private String channelId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DeliveryStage deliveryStage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DeliveryStatus deliveryStatus;

    @Column(length = 64)
    private String errorCode;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime occurredAt;

    @Builder
    public DeliveryEvent(UUID messageId, String traceId, UUID jobId, String orgId,
                         String userId, String programId, String channelId,
                         DeliveryStage deliveryStage, DeliveryStatus deliveryStatus,
                         String errorCode, String errorMessage) {
        this.messageId = messageId;
        this.traceId = traceId;
        this.jobId = jobId;
        this.orgId = orgId;
        this.userId = userId;
        this.programId = programId;
        this.channelId = channelId;
        this.deliveryStage = deliveryStage;
        this.deliveryStatus = deliveryStatus;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
