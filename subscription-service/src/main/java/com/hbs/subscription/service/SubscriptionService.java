package com.hbs.subscription.service;

import com.hbs.subscription.cache.SubscriptionCacheService;
import com.hbs.subscription.dto.request.SubscribeRequest;
import com.hbs.subscription.dto.response.SubscribeResponse;
import com.hbs.subscription.entity.Subscription;
import com.hbs.subscription.enums.SubscriptionStatus;
import com.hbs.subscription.exception.DuplicateSubscriptionException;
import com.hbs.subscription.exception.SubscriptionNotFoundException;
import com.hbs.subscription.repository.SubscriptionRepository;
import com.hbs.tracking.dto.TrackingEventCommand;
import com.hbs.tracking.enums.TrackingEventType;
import com.hbs.tracking.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private static final String SERVICE_NAME = "subscription-service";

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionCacheService cacheService;
    private final TrackingService trackingService;

    @Transactional
    public SubscribeResponse subscribe(String orgId, String userId, String traceId,
                                       SubscribeRequest request) {
        Subscription subscription = Subscription.builder()
                .orgId(orgId)
                .userId(userId)
                .programId(request.programId())
                .channelId(request.channelId())
                .build();
        try {
            subscriptionRepository.save(subscription);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateSubscriptionException(request.programId(), request.channelId());
        }

        cacheService.addSubscription(orgId, userId, request.programId(), request.channelId());

        trackingService.recordEvent(TrackingEventCommand.of(
                traceId, TrackingEventType.SUBSCRIPTION_CREATED,
                SERVICE_NAME, orgId, "SUBSCRIPTION", String.valueOf(subscription.getId()), "SUCCESS"
        ));

        log.info("[Subscription] 구독 생성: userId={}, programId={}, channelId={}",
                userId, request.programId(), request.channelId());

        return SubscribeResponse.from(subscription);
    }

    @Transactional
    public void unsubscribe(Long subscriptionId, String userId, String orgId, String traceId) {
        Subscription subscription = findOwnedSubscription(subscriptionId, userId);

        subscriptionRepository.delete(subscription);
        cacheService.removeSubscription(orgId, userId, subscription.getProgramId(),
                subscription.getChannelId());

        trackingService.recordEvent(TrackingEventCommand.of(
                traceId, TrackingEventType.SUBSCRIPTION_DELETED,
                SERVICE_NAME, orgId, "SUBSCRIPTION", String.valueOf(subscriptionId), "SUCCESS"
        ));

        log.info("[Subscription] 구독 삭제: subscriptionId={}, userId={}", subscriptionId, userId);
    }

    @Transactional
    public SubscribeResponse pause(Long subscriptionId, String userId, String orgId, String traceId) {
        Subscription subscription = findOwnedSubscription(subscriptionId, userId);

        subscription.pause();
        cacheService.removeSubscription(orgId, userId, subscription.getProgramId(),
                subscription.getChannelId());

        trackingService.recordEvent(TrackingEventCommand.of(
                traceId, TrackingEventType.SUBSCRIPTION_PAUSED,
                SERVICE_NAME, orgId, "SUBSCRIPTION", String.valueOf(subscriptionId), "SUCCESS"
        ));

        log.info("[Subscription] 구독 일시정지: subscriptionId={}, userId={}", subscriptionId, userId);

        return SubscribeResponse.from(subscription);
    }

    @Transactional
    public SubscribeResponse resume(Long subscriptionId, String userId, String orgId, String traceId) {
        Subscription subscription = findOwnedSubscription(subscriptionId, userId);

        subscription.resume();
        cacheService.addSubscription(orgId, userId, subscription.getProgramId(),
                subscription.getChannelId());

        trackingService.recordEvent(TrackingEventCommand.of(
                traceId, TrackingEventType.SUBSCRIPTION_RESUMED,
                SERVICE_NAME, orgId, "SUBSCRIPTION", String.valueOf(subscriptionId), "SUCCESS"
        ));

        log.info("[Subscription] 구독 재개: subscriptionId={}, userId={}", subscriptionId, userId);

        return SubscribeResponse.from(subscription);
    }

    @Transactional(readOnly = true)
    public List<SubscribeResponse> listByUser(String orgId, String userId) {
        return subscriptionRepository.findByOrgIdAndUserId(orgId, userId)
                .stream()
                .map(SubscribeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubscribeResponse> listActiveByUser(String orgId, String userId) {
        return subscriptionRepository.findByOrgIdAndUserIdAndStatus(orgId, userId,
                        SubscriptionStatus.ACTIVE)
                .stream()
                .map(SubscribeResponse::from)
                .toList();
    }

    private Subscription findOwnedSubscription(Long id, String userId) {
        return subscriptionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new SubscriptionNotFoundException(id));
    }
}
