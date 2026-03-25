package com.hbs.ingestion.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * SSCAN 기반 구독자 resolve 서비스.
 *
 * 키 구조: sub:{org}:{program}:{channel}:{shard} → SET(userId)
 * SMEMBERS 대신 SSCAN을 사용해 대량 Set을 스트리밍으로 처리한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriberResolveService {

    private static final int SHARD_COUNT = 100;
    private static final int SCAN_COUNT = 200;

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 특정 프로그램/채널의 모든 샤드를 SSCAN으로 스캔해 구독자 userId 목록을 반환한다.
     * channelId가 null이면 "all" 샤드를 조회한다.
     */
    public List<String> resolveSubscribers(String orgId, String programId, String channelId) {
        String effectiveChannel = channelId != null ? channelId : "all";
        List<String> result = new ArrayList<>();

        for (int shard = 0; shard < SHARD_COUNT; shard++) {
            String key = String.format("sub:%s:%s:%s:%02d", orgId, programId, effectiveChannel, shard);
            scanSet(key, result);
        }

        log.debug("[SubscriberResolve] org={}, program={}, channel={}, total={}",
                orgId, programId, effectiveChannel, result.size());
        return result;
    }

    private void scanSet(String key, List<String> accumulator) {
        ScanOptions options = ScanOptions.scanOptions().count(SCAN_COUNT).build();
        try (Cursor<String> cursor = redisTemplate.opsForSet().scan(key, options)) {
            while (cursor.hasNext()) {
                accumulator.add(cursor.next());
            }
        } catch (Exception e) {
            log.warn("[SubscriberResolve] SSCAN 실패: key={}, error={}", key, e.getMessage());
        }
    }
}
