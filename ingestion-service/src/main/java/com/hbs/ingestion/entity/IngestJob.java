package com.hbs.ingestion.entity;

import com.hbs.ingestion.enums.IngestJobStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ingest_jobs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IngestJob {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private IngestJobStatus status;

    @Column(nullable = false, length = 64)
    private String orgId;

    @Column(nullable = false, length = 64)
    private String programId;

    @Column(length = 64)
    private String channelId;

    @Column(nullable = false, length = 32)
    private String type;

    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(nullable = false, length = 64)
    private String traceId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @Column(columnDefinition = "TEXT")
    private String errorMsg;

    @Builder
    public IngestJob(String orgId, String programId, String channelId,
                     String type, String payload, String traceId) {
        this.id = UUID.randomUUID();
        this.status = IngestJobStatus.PENDING;
        this.orgId = orgId;
        this.programId = programId;
        this.channelId = channelId;
        this.type = type;
        this.payload = payload;
        this.traceId = traceId;
    }

    public void startRunning() {
        this.status = IngestJobStatus.RUNNING;
    }

    public void complete() {
        this.status = IngestJobStatus.DONE;
    }

    public void fail(String errorMsg) {
        this.status = IngestJobStatus.FAILED;
        this.errorMsg = errorMsg;
    }
}
