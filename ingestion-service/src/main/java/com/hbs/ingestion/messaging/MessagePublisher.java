package com.hbs.ingestion.messaging;

import com.hbs.tracking.model.MessageEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessagePublisher {

    private static final String HEADER_TRACE_ID = "x-trace-id";
    private static final String HEADER_MESSAGE_ID = "x-message-id";
    private static final String HEADER_JOB_ID = "x-job-id";

    @Value("${hbs.kafka.topic.user-messages:user-messages}")
    private String topic;

    private final KafkaTemplate<String, MessageEnvelope> kafkaTemplate;

    /**
     * userId를 Kafka key로 사용해 메시지를 발행한다.
     * 트레이스 헤더(x-trace-id, x-message-id, x-job-id)를 함께 전파한다.
     */
    public void publish(MessageEnvelope envelope, String jobId) {
        ProducerRecord<String, MessageEnvelope> record = new ProducerRecord<>(
                topic, envelope.userId(), envelope);

        record.headers().add(new RecordHeader(HEADER_TRACE_ID,
                envelope.traceId().getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader(HEADER_MESSAGE_ID,
                envelope.messageId().getBytes(StandardCharsets.UTF_8)));
        if (jobId != null) {
            record.headers().add(new RecordHeader(HEADER_JOB_ID,
                    jobId.getBytes(StandardCharsets.UTF_8)));
        }

        kafkaTemplate.send(record)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[Publisher] Kafka 발행 실패: messageId={}, userId={}, error={}",
                                envelope.messageId(), envelope.userId(), ex.getMessage());
                    } else {
                        log.debug("[Publisher] Kafka 발행 완료: messageId={}, userId={}, offset={}",
                                envelope.messageId(), envelope.userId(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
