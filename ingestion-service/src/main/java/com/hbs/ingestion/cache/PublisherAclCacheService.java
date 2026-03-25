package com.hbs.ingestion.cache;

import com.hbs.ingestion.entity.PublisherAclReadModel;
import com.hbs.ingestion.repository.PublisherAclReadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * 발행 권한(publisher_acl) Redis 캐시 서비스.
 *
 * 키 구조:
 *   pubAcl:{org}:{subjectType}:{subjectId} -> SET(program:channel)
 *
 * 캐시 미스 시 DB에서 로드 후 저장. TTL: 5분.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PublisherAclCacheService {

    private static final Duration CACHE_TTL = Duration.ofMinutes(5);
    private static final String CACHE_EMPTY_SENTINEL = "__EMPTY__";

    private final RedisTemplate<String, String> redisTemplate;
    private final PublisherAclReadRepository aclRepository;

    public boolean hasPermission(String orgId, String subjectType, String subjectId,
                                  String programId, String channelId) {
        String cacheKey = cacheKey(orgId, subjectType, subjectId);
        String effectiveChannel = channelId != null ? channelId : "all";

        Set<String> cached = redisTemplate.opsForSet().members(cacheKey);

        if (cached == null || cached.isEmpty()) {
            cached = loadFromDb(orgId, subjectType, subjectId, cacheKey);
        }

        if (cached.contains(CACHE_EMPTY_SENTINEL)) {
            return false;
        }

        // program:channel 또는 program:all (전체 채널 권한) 중 하나라도 있으면 허용
        return cached.contains(programId + ":" + effectiveChannel)
                || cached.contains(programId + ":all");
    }

    private Set<String> loadFromDb(String orgId, String subjectType, String subjectId,
                                    String cacheKey) {
        List<PublisherAclReadModel> acls = aclRepository.findActiveAcls(orgId, subjectType, subjectId);

        if (acls.isEmpty()) {
            redisTemplate.opsForSet().add(cacheKey, CACHE_EMPTY_SENTINEL);
            redisTemplate.expire(cacheKey, CACHE_TTL);
            log.debug("[AclCache] DB 조회 결과 없음: key={}", cacheKey);
            return Set.of(CACHE_EMPTY_SENTINEL);
        }

        String[] entries = acls.stream()
                .map(a -> a.getProgramId() + ":" + (a.getChannelId() != null ? a.getChannelId() : "all"))
                .toArray(String[]::new);

        redisTemplate.opsForSet().add(cacheKey, entries);
        redisTemplate.expire(cacheKey, CACHE_TTL);

        log.debug("[AclCache] DB 로드 완료: key={}, entries={}", cacheKey, entries.length);
        return redisTemplate.opsForSet().members(cacheKey);
    }

    private String cacheKey(String orgId, String subjectType, String subjectId) {
        return String.format("pubAcl:%s:%s:%s", orgId, subjectType, subjectId);
    }
}
