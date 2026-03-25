package com.hbs.subscription.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SubscribeRequest(
        @NotBlank(message = "programId는 필수입니다.") String programId,
        String channelId  // null이면 프로그램 전체 채널 구독
) {}
