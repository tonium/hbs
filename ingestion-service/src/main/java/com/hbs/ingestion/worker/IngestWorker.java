package com.hbs.ingestion.worker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hbs.ingestion.cache.SubscriberResolveService;
import com.hbs.ingestion.entity.IngestJob;
import com.hbs.ingestion.messaging.MessagePublisher;
import com.hbs.ingestion.repository.IngestJobRepository;
import com.hbs.tracking.dto.DeliveryEventCommand;
import com.hbs.tracking.dto.TrackingEventCommand;
import com.hbs.tracking.enums.DeliveryStage;
import com.hbs.tracking.enums.DeliveryStatus;
import com.hbs.tracking.enums.TrackingEventType;
import com.hbs.tracking.model.MessageEnvelope;
import com.hbs.tracking.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * PENDING 상태의 IngestJob을 polling하여 구독자 fan-out을 처리하는 워커.
 * FOR UPDATE SKIP LOCKED 쿼리로 다중 인스턴스 중복 처리를 방지한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IngestWorker {

    private static final int BATCH_SIZE = 10;

    private final IngestJobRepository jobRepository;
    private final SubscriberResolveService subscriberResolveService;
    private final MessagePublisher messagePublisher;
    private final TrackingService trackingService;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void process() {
        List<IngestJob> jobs = jobRepository.findPendingJobsForUpdate(BATCH_SIZE);
        for (IngestJob job : jobs) {
            processJob(job);
        }
    }

    private void processJob(IngestJob job) {
        job.startRunning();
        log.info("[IngestWorker] Job 시작: jobId={}, org={}, program={}", job.getId(), job.getOrgId(), job.getProgramId());

        trackingService.recordEvent(TrackingEventCommand.of(
                job.getTraceId(), TrackingEventType.MESSAGE_FANOUT_STARTED,
                "ingestion-service", job.getOrgId(), "JOB", job.getId().toString(),
                DeliveryStatus.PENDING.name()));

        try {
            Map<String, Object> payload = deserializePayload(job.getPayload());
            List<String> subscribers = subscriberResolveService.resolveSubscribers(
                    job.getOrgId(), job.getProgramId(), job.getChannelId());

            log.info("[IngestWorker] 구독자 resolve 완료: jobId={}, count={}", job.getId(), subscribers.size());

            int published = 0;
            for (String userId : subscribers) {
                MessageEnvelope envelope = MessageEnvelope.create(
                        job.getOrgId(), userId, job.getProgramId(), job.getChannelId(),
                        job.getType(), 0, job.getTraceId(), payload);

                messagePublisher.publish(envelope, job.getId().toString());

                trackingService.recordDelivery(DeliveryEventCommand.success(
                        java.util.UUID.fromString(envelope.messageId()), job.getTraceId(), job.getId(),
                        job.getOrgId(), userId, job.getProgramId(), job.getChannelId(),
                        DeliveryStage.KAFKA_PUBLISH));

                published++;
            }

            job.complete();
            log.info("[IngestWorker] Job 완료: jobId={}, published={}", job.getId(), published);

            trackingService.recordEvent(TrackingEventCommand.of(
                    job.getTraceId(), TrackingEventType.MESSAGE_PUBLISHED_TO_KAFKA,
                    "ingestion-service", job.getOrgId(), "JOB", job.getId().toString(),
                    DeliveryStatus.SUCCESS.name()));

        } catch (Exception e) {
            job.fail(e.getMessage());
            log.error("[IngestWorker] Job 실패: jobId={}, error={}", job.getId(), e.getMessage(), e);
        }
    }

    private Map<String, Object> deserializePayload(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalStateException("payload 역직렬화 실패", e);
        }
    }
}
