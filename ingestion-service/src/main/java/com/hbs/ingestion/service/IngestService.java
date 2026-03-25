package com.hbs.ingestion.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hbs.ingestion.dto.request.PublishRequest;
import com.hbs.ingestion.dto.response.AcceptResponse;
import com.hbs.ingestion.entity.IngestJob;
import com.hbs.ingestion.repository.IngestJobRepository;
import com.hbs.tracking.dto.TrackingEventCommand;
import com.hbs.tracking.enums.DeliveryStatus;
import com.hbs.tracking.enums.TrackingEventType;
import com.hbs.tracking.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestService {

    private final IngestJobRepository jobRepository;
    private final PublisherAclService aclService;
    private final TrackingService trackingService;
    private final ObjectMapper objectMapper;

    /**
     * 발행 권한을 검증하고 IngestJob을 생성한다.
     * 대량 fan-out은 비동기 worker가 처리하므로 즉시 202 Accepted 반환.
     */
    @Transactional
    public AcceptResponse accept(String orgId, String subjectId, String subjectType,
                                  PublishRequest request, String traceId) {
        aclService.assertPermission(orgId, subjectType, subjectId,
                request.programId(), request.channelId());

        String payloadJson = serializePayload(request);

        IngestJob job = IngestJob.builder()
                .orgId(orgId)
                .programId(request.programId())
                .channelId(request.channelId())
                .type(request.type())
                .payload(payloadJson)
                .traceId(traceId)
                .build();

        jobRepository.save(job);

        trackingService.recordEvent(TrackingEventCommand.of(
                traceId, TrackingEventType.MESSAGE_INGEST_ACCEPTED,
                "ingestion-service", orgId, "JOB", job.getId().toString(),
                DeliveryStatus.PENDING.name()));

        log.info("[IngestService] Job 생성 완료: jobId={}, orgId={}, program={}, channel={}, traceId={}",
                job.getId(), orgId, request.programId(), request.channelId(), traceId);

        return new AcceptResponse(job.getId().toString(), traceId);
    }

    private String serializePayload(PublishRequest request) {
        try {
            return objectMapper.writeValueAsString(request.payload());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("payload 직렬화 실패", e);
        }
    }
}
