package com.hbs.subscription.exception;

public class DuplicateSubscriptionException extends RuntimeException {

    public DuplicateSubscriptionException(String programId, String channelId) {
        super(String.format("이미 구독 중입니다. programId=%s, channelId=%s", programId, channelId));
    }
}
