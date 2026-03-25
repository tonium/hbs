package com.hbs.ingestion.exception;

public class PublishPermissionDeniedException extends RuntimeException {

    public PublishPermissionDeniedException(String subjectId, String programId, String channelId) {
        super(String.format("발행 권한이 없습니다. subjectId=%s, programId=%s, channelId=%s",
                subjectId, programId, channelId));
    }
}
