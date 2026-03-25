package com.hbs.subscription.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 구독 정보 Redis write-through 캐시 서비스.
 *
 * 키 구조:
 *   sub:{org}:{program}:{channel}:{shard} -> SET(userId)   구독자 집합 (샤딩)
 *   uSub:{org}:{userId}                  -> SET(program:channel)  사용자 구독 목록
 *   permVer:{org}:{userId}               -> integer        권한 버전 (캐시 무효화 트리거)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionCacheService {

    private static final int SHARD_COUNT = 100;

    private final RedisTemplate<String, String> redisTemplate;

    public void addSubscription(String orgId, String userId, String programId, String channelId) {
        String effectiveChannel = effectiveChannel(channelId);
        String shardKey = subKey(orgId, programId, effectiveChannel, userId);
        String uSubKey = uSubKey(orgId, userId);
        String programChannel = programId + ":" + effectiveChannel;

        redisTemplate.opsForSet().add(shardKey, userId);
        redisTemplate.opsForSet().add(uSubKey, programChannel);
        incrementPermVersion(orgId, userId);

        log.debug("[Cache] 구독 추가: key={}, userId={}", shardKey, userId);
    }

    public void removeSubscription(String orgId, String userId, String programId, String channelId) {
        String effectiveChannel = effectiveChannel(channelId);
        String shardKey = subKey(orgId, programId, effectiveChannel, userId);
        String uSubKey = uSubKey(orgId, userId);
        String programChannel = programId + ":" + effectiveChannel;

        redisTemplate.opsForSet().remove(shardKey, userId);
        redisTemplate.opsForSet().remove(uSubKey, programChannel);
        incrementPermVersion(orgId, userId);

        log.debug("[Cache] 구독 제거: key={}, userId={}", shardKey, userId);
    }

    private void incrementPermVersion(String orgId, String userId) {
        String permVerKey = String.format("permVer:%s:%s", orgId, userId);
        redisTemplate.opsForValue().increment(permVerKey);
    }

    private String subKey(String orgId, String programId, String channel, String userId) {
        int shard = Math.floorMod(userId.hashCode(), SHARD_COUNT);
        return String.format("sub:%s:%s:%s:%02d", orgId, programId, channel, shard);
    }

    private String uSubKey(String orgId, String userId) {
        return String.format("uSub:%s:%s", orgId, userId);
    }

    private String effectiveChannel(String channelId) {
        return (channelId != null && !channelId.isBlank()) ? channelId : "all";
    }
}
