package com.hbs.tracking.service;

import com.hbs.tracking.dto.DeliveryEventCommand;
import com.hbs.tracking.dto.TrackingEventCommand;
import com.hbs.tracking.entity.DeliveryEvent;
import com.hbs.tracking.entity.EventLog;
import com.hbs.tracking.repository.DeliveryEventRepository;
import com.hbs.tracking.repository.EventLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingService {

    private final EventLogRepository eventLogRepository;
    private final DeliveryEventRepository deliveryEventRepository;

    @Transactional
    public void recordEvent(TrackingEventCommand command) {
        try {
            EventLog eventLog = EventLog.builder()
                    .traceId(command.traceId())
                    .spanId(command.spanId())
                    .eventType(command.eventType())
                    .serviceName(command.serviceName())
                    .orgId(command.orgId())
                    .actorType(command.actorType())
                    .actorId(command.actorId())
                    .targetType(command.targetType())
                    .targetId(command.targetId())
                    .status(command.status())
                    .payload(command.payload())
                    .build();
            eventLogRepository.save(eventLog);
        } catch (Exception e) {
            // 트래킹 실패가 비즈니스 로직을 방해하지 않도록 로그만 기록
            log.error("[Tracking] 이벤트 기록 실패: eventType={}, traceId={}, error={}",
                    command.eventType(), command.traceId(), e.getMessage());
        }
    }

    @Transactional
    public void recordDelivery(DeliveryEventCommand command) {
        try {
            DeliveryEvent deliveryEvent = DeliveryEvent.builder()
                    .messageId(command.messageId())
                    .traceId(command.traceId())
                    .jobId(command.jobId())
                    .orgId(command.orgId())
                    .userId(command.userId())
                    .programId(command.programId())
                    .channelId(command.channelId())
                    .deliveryStage(command.deliveryStage())
                    .deliveryStatus(command.deliveryStatus())
                    .errorCode(command.errorCode())
                    .errorMessage(command.errorMessage())
                    .build();
            deliveryEventRepository.save(deliveryEvent);
        } catch (Exception e) {
            log.error("[Tracking] 전달 이벤트 기록 실패: messageId={}, stage={}, error={}",
                    command.messageId(), command.deliveryStage(), e.getMessage());
        }
    }
}
