package com.hbs.tracking.enums;

public enum DeliveryStage {
    INGEST,
    FANOUT,
    KAFKA_PUBLISH,
    GATEWAY_RECEIVE,
    SSE_PUSH,
    REDELIVERY
}
