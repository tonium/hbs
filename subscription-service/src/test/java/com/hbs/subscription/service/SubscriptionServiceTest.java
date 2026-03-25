package com.hbs.subscription.service;

import com.hbs.subscription.cache.SubscriptionCacheService;
import com.hbs.subscription.dto.request.SubscribeRequest;
import com.hbs.subscription.dto.response.SubscribeResponse;
import com.hbs.subscription.entity.Subscription;
import com.hbs.subscription.enums.SubscriptionStatus;
import com.hbs.subscription.exception.DuplicateSubscriptionException;
import com.hbs.subscription.exception.SubscriptionNotFoundException;
import com.hbs.subscription.repository.SubscriptionRepository;
import com.hbs.tracking.service.TrackingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private SubscriptionCacheService cacheService;
    @Mock
    private TrackingService trackingService;

    private static final String ORG_ID = "org-001";
    private static final String USER_ID = "user-001";
    private static final String TRACE_ID = "trace-001";

    @Test
    @DisplayName("subscribe - 정상 구독 생성")
    void subscribe_success() {
        // given
        SubscribeRequest request = new SubscribeRequest("health", "notice");
        given(subscriptionRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        // when
        SubscribeResponse response = subscriptionService.subscribe(ORG_ID, USER_ID, TRACE_ID, request);

        // then
        assertThat(response.orgId()).isEqualTo(ORG_ID);
        assertThat(response.userId()).isEqualTo(USER_ID);
        assertThat(response.programId()).isEqualTo("health");
        assertThat(response.channelId()).isEqualTo("notice");
        assertThat(response.status()).isEqualTo("ACTIVE");

        then(cacheService).should().addSubscription(ORG_ID, USER_ID, "health", "notice");
        then(trackingService).should().recordEvent(any());
    }

    @Test
    @DisplayName("subscribe - 중복 구독 시 DuplicateSubscriptionException 발생")
    void subscribe_duplicate_throwsException() {
        // given
        SubscribeRequest request = new SubscribeRequest("health", "notice");
        given(subscriptionRepository.save(any())).willThrow(DataIntegrityViolationException.class);

        // when & then
        assertThatThrownBy(() ->
                subscriptionService.subscribe(ORG_ID, USER_ID, TRACE_ID, request))
                .isInstanceOf(DuplicateSubscriptionException.class);
    }

    @Test
    @DisplayName("unsubscribe - 정상 구독 삭제")
    void unsubscribe_success() {
        // given
        Subscription subscription = Subscription.builder()
                .orgId(ORG_ID).userId(USER_ID).programId("health").channelId("notice").build();
        given(subscriptionRepository.findByIdAndUserId(1L, USER_ID))
                .willReturn(Optional.of(subscription));

        // when
        subscriptionService.unsubscribe(1L, USER_ID, ORG_ID, TRACE_ID);

        // then
        then(subscriptionRepository).should().delete(subscription);
        then(cacheService).should().removeSubscription(ORG_ID, USER_ID, "health", "notice");
    }

    @Test
    @DisplayName("unsubscribe - 존재하지 않는 구독 삭제 시 SubscriptionNotFoundException 발생")
    void unsubscribe_notFound_throwsException() {
        // given
        given(subscriptionRepository.findByIdAndUserId(99L, USER_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                subscriptionService.unsubscribe(99L, USER_ID, ORG_ID, TRACE_ID))
                .isInstanceOf(SubscriptionNotFoundException.class);
    }

    @Test
    @DisplayName("pause - 정상 일시정지 및 캐시 제거")
    void pause_success() {
        // given
        Subscription subscription = Subscription.builder()
                .orgId(ORG_ID).userId(USER_ID).programId("health").channelId("notice").build();
        given(subscriptionRepository.findByIdAndUserId(1L, USER_ID))
                .willReturn(Optional.of(subscription));

        // when
        SubscribeResponse response = subscriptionService.pause(1L, USER_ID, ORG_ID, TRACE_ID);

        // then
        assertThat(response.status()).isEqualTo(SubscriptionStatus.PAUSED.name());
        then(cacheService).should().removeSubscription(ORG_ID, USER_ID, "health", "notice");
    }

    @Test
    @DisplayName("resume - 정상 재개 및 캐시 추가")
    void resume_success() {
        // given
        Subscription subscription = Subscription.builder()
                .orgId(ORG_ID).userId(USER_ID).programId("health").channelId("notice").build();
        subscription.pause();
        given(subscriptionRepository.findByIdAndUserId(1L, USER_ID))
                .willReturn(Optional.of(subscription));

        // when
        SubscribeResponse response = subscriptionService.resume(1L, USER_ID, ORG_ID, TRACE_ID);

        // then
        assertThat(response.status()).isEqualTo(SubscriptionStatus.ACTIVE.name());
        then(cacheService).should().addSubscription(ORG_ID, USER_ID, "health", "notice");
    }

    @Test
    @DisplayName("listByUser - 사용자 구독 목록 조회")
    void listByUser_returnsList() {
        // given
        Subscription sub1 = Subscription.builder()
                .orgId(ORG_ID).userId(USER_ID).programId("health").channelId("notice").build();
        Subscription sub2 = Subscription.builder()
                .orgId(ORG_ID).userId(USER_ID).programId("health").channelId(null).build();
        given(subscriptionRepository.findByOrgIdAndUserId(ORG_ID, USER_ID))
                .willReturn(List.of(sub1, sub2));

        // when
        List<SubscribeResponse> responses = subscriptionService.listByUser(ORG_ID, USER_ID);

        // then
        assertThat(responses).hasSize(2);
    }
}
