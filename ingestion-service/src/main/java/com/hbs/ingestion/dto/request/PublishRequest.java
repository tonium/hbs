package com.hbs.ingestion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Map;

public record PublishRequest(
        @NotBlank(message = "programId는 필수입니다.") String programId,
        String channelId,           // null이면 프로그램 전체 구독자 대상
        @NotBlank(message = "type은 필수입니다.") String type,
        @NotNull(message = "payload는 필수입니다.") Map<String, Object> payload,
        @Positive(message = "ttlSeconds는 양수여야 합니다.") int ttlSeconds
) {}
