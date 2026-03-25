package com.hbs.tracking.repository;

import com.hbs.tracking.entity.DeliveryEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeliveryEventRepository extends JpaRepository<DeliveryEvent, Long> {

    List<DeliveryEvent> findByMessageId(UUID messageId);

    List<DeliveryEvent> findByUserIdOrderByOccurredAtDesc(String userId);

    List<DeliveryEvent> findByTraceId(String traceId);
}
