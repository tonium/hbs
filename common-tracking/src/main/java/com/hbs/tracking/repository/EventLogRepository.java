package com.hbs.tracking.repository;

import com.hbs.tracking.entity.EventLog;
import com.hbs.tracking.enums.TrackingEventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventLogRepository extends JpaRepository<EventLog, Long> {

    List<EventLog> findByTraceId(String traceId);

    List<EventLog> findByTargetTypeAndTargetId(String targetType, String targetId);

    List<EventLog> findByEventTypeOrderByOccurredAtDesc(TrackingEventType eventType);
}
