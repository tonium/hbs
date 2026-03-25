package com.hbs.ingestion.service;

import com.hbs.ingestion.cache.PublisherAclCacheService;
import com.hbs.ingestion.exception.PublishPermissionDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublisherAclService {

    private final PublisherAclCacheService aclCacheService;

    /**
     * 발행 권한을 확인하고, 권한이 없으면 예외를 던진다.
     *
     * @param orgId       조직 ID
     * @param subjectType USER 또는 SERVICE
     * @param subjectId   JWT sub (userId) 또는 서비스 계정 ID
     * @param programId   발행 대상 프로그램
     * @param channelId   발행 대상 채널 (null이면 프로그램 전체)
     */
    public void assertPermission(String orgId, String subjectType, String subjectId,
                                  String programId, String channelId) {
        boolean allowed = aclCacheService.hasPermission(orgId, subjectType, subjectId,
                programId, channelId);
        if (!allowed) {
            throw new PublishPermissionDeniedException(subjectId, programId, channelId);
        }
    }
}
