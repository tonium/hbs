package com.hbs.subscription.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hbs.security.config.SecurityConfig;
import com.hbs.subscription.dto.request.SubscribeRequest;
import com.hbs.subscription.dto.response.SubscribeResponse;
import com.hbs.subscription.exception.DuplicateSubscriptionException;
import com.hbs.subscription.exception.GlobalExceptionHandler;
import com.hbs.subscription.service.SubscriptionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubscriptionController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@SuppressWarnings("java:S100") // 테스트 메서드명은 given_when_then 패턴 허용
class SubscriptionControllerTest {

    private static final String URL_SUBSCRIPTIONS = "/subscriptions";
    private static final String PROGRAM_ID = "health";
    private static final String CHANNEL_ID = "notice";
    private static final String USER_ID = "user-001";
    private static final String ORG_ID = "org-001";
    private static final String ROLE_SUBSCRIBE = "SUBSCRIBE";
    private static final String CLAIM_ORG_ID = "orgId";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SubscriptionService subscriptionService;

    @MockitoBean
    private JwtDecoder jwtDecoder; // Keycloak 연결 없이 테스트하기 위한 Mock

    @Test
    @DisplayName("POST /subscriptions - 구독 생성 성공 (201)")
    void subscribe_success() throws Exception {
        SubscribeRequest request = new SubscribeRequest(PROGRAM_ID, CHANNEL_ID);
        SubscribeResponse response = new SubscribeResponse(
                1L, ORG_ID, USER_ID, PROGRAM_ID, CHANNEL_ID, "ACTIVE", OffsetDateTime.now());

        given(subscriptionService.subscribe(eq(ORG_ID), eq(USER_ID), any(), eq(request)))
                .willReturn(response);

        mockMvc.perform(post(URL_SUBSCRIPTIONS)
                        .with(jwt()
                                .jwt(b -> b.subject(USER_ID).claim(CLAIM_ORG_ID, ORG_ID))
                                .authorities(role(ROLE_SUBSCRIBE)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.programId").value(PROGRAM_ID))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("POST /subscriptions - 인증 없이 요청 시 401")
    void subscribe_unauthenticated_returns401() throws Exception {
        SubscribeRequest request = new SubscribeRequest(PROGRAM_ID, CHANNEL_ID);

        mockMvc.perform(post(URL_SUBSCRIPTIONS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /subscriptions - ROLE_SUBSCRIBE 없이 요청 시 403")
    void subscribe_forbidden_returns403() throws Exception {
        SubscribeRequest request = new SubscribeRequest(PROGRAM_ID, CHANNEL_ID);

        mockMvc.perform(post(URL_SUBSCRIPTIONS)
                        .with(jwt()
                                .jwt(b -> b.subject(USER_ID).claim(CLAIM_ORG_ID, ORG_ID))
                                .authorities(role("USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /subscriptions - 중복 구독 시 409")
    void subscribe_duplicate_returns409() throws Exception {
        SubscribeRequest request = new SubscribeRequest(PROGRAM_ID, CHANNEL_ID);
        given(subscriptionService.subscribe(any(), any(), any(), any()))
                .willThrow(new DuplicateSubscriptionException(PROGRAM_ID, CHANNEL_ID));

        mockMvc.perform(post(URL_SUBSCRIPTIONS)
                        .with(jwt()
                                .jwt(b -> b.subject(USER_ID).claim(CLAIM_ORG_ID, ORG_ID))
                                .authorities(role(ROLE_SUBSCRIBE)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_SUBSCRIPTION"));
    }

    @Test
    @DisplayName("DELETE /subscriptions/{id} - 구독 삭제 성공 (200)")
    void unsubscribe_success() throws Exception {
        willDoNothing().given(subscriptionService).unsubscribe(any(), any(), any(), any());

        mockMvc.perform(delete(URL_SUBSCRIPTIONS + "/1")
                        .with(jwt()
                                .jwt(b -> b.subject(USER_ID).claim(CLAIM_ORG_ID, ORG_ID))
                                .authorities(role(ROLE_SUBSCRIBE))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /subscriptions - 구독 목록 조회 성공 (200)")
    void list_success() throws Exception {
        given(subscriptionService.listByUser(ORG_ID, USER_ID)).willReturn(List.of());

        mockMvc.perform(get(URL_SUBSCRIPTIONS)
                        .with(jwt()
                                .jwt(b -> b.subject(USER_ID).claim(CLAIM_ORG_ID, ORG_ID))
                                .authorities(role(ROLE_SUBSCRIBE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("POST /subscriptions - programId 누락 시 400")
    void subscribe_missingProgramId_returns400() throws Exception {
        String invalidRequest = """
                {"programId": "", "channelId": "notice"}
                """;

        mockMvc.perform(post(URL_SUBSCRIPTIONS)
                        .with(jwt()
                                .jwt(b -> b.subject(USER_ID).claim(CLAIM_ORG_ID, ORG_ID))
                                .authorities(role(ROLE_SUBSCRIBE)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
    }

    private SimpleGrantedAuthority role(String role) {
        return new SimpleGrantedAuthority("ROLE_" + role);
    }
}
