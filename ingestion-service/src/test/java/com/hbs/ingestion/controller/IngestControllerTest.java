package com.hbs.ingestion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hbs.ingestion.dto.request.PublishRequest;
import com.hbs.ingestion.dto.response.AcceptResponse;
import com.hbs.ingestion.exception.GlobalExceptionHandler;
import com.hbs.ingestion.exception.PublishPermissionDeniedException;
import com.hbs.ingestion.service.IngestService;
import com.hbs.security.config.SecurityConfig;
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

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("java:S100")
@WebMvcTest(IngestController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class IngestControllerTest {

    private static final String URL_MESSAGES = "/messages";
    private static final String PROGRAM_ID = "prog1";
    private static final String CHANNEL_ID = "ch1";
    private static final String USER_ID = "user-001";
    private static final String ORG_ID = "org-001";
    private static final String ROLE_PUBLISH = "PUBLISH";
    private static final String CLAIM_ORG_ID = "orgId";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IngestService ingestService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("POST /messages - 발행 성공 (202)")
    void publish_success_returns202() throws Exception {
        PublishRequest request = new PublishRequest(PROGRAM_ID, CHANNEL_ID, "ALERT",
                Map.of("msg", "hello"), 60);
        AcceptResponse response = new AcceptResponse(UUID.randomUUID().toString(), "trace-123");

        given(ingestService.accept(any(), any(), any(), any(), any())).willReturn(response);

        mockMvc.perform(post(URL_MESSAGES)
                        .with(jwt()
                                .jwt(b -> b.subject(USER_ID).claim(CLAIM_ORG_ID, ORG_ID))
                                .authorities(role(ROLE_PUBLISH)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.jobId").isNotEmpty())
                .andExpect(jsonPath("$.data.traceId").value("trace-123"));
    }

    @Test
    @DisplayName("POST /messages - 인증 없이 요청 시 401")
    void publish_unauthenticated_returns401() throws Exception {
        PublishRequest request = new PublishRequest(PROGRAM_ID, CHANNEL_ID, "ALERT",
                Map.of("msg", "hello"), 60);

        mockMvc.perform(post(URL_MESSAGES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /messages - ROLE_PUBLISH 없이 요청 시 403")
    void publish_forbidden_returns403() throws Exception {
        PublishRequest request = new PublishRequest(PROGRAM_ID, CHANNEL_ID, "ALERT",
                Map.of("msg", "hello"), 60);

        mockMvc.perform(post(URL_MESSAGES)
                        .with(jwt()
                                .jwt(b -> b.subject(USER_ID).claim(CLAIM_ORG_ID, ORG_ID))
                                .authorities(role("USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /messages - 발행 권한 없을 시 403")
    void publish_noAclPermission_returns403() throws Exception {
        PublishRequest request = new PublishRequest(PROGRAM_ID, CHANNEL_ID, "ALERT",
                Map.of("msg", "hello"), 60);

        given(ingestService.accept(any(), any(), any(), any(), any()))
                .willThrow(new PublishPermissionDeniedException(USER_ID, PROGRAM_ID, CHANNEL_ID));

        mockMvc.perform(post(URL_MESSAGES)
                        .with(jwt()
                                .jwt(b -> b.subject(USER_ID).claim(CLAIM_ORG_ID, ORG_ID))
                                .authorities(role(ROLE_PUBLISH)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("PUBLISH_PERMISSION_DENIED"));
    }

    @Test
    @DisplayName("POST /messages - programId 누락 시 400")
    void publish_missingProgramId_returns400() throws Exception {
        String invalidRequest = """
                {"programId": "", "type": "ALERT", "payload": {}, "ttlSeconds": 60}
                """;

        mockMvc.perform(post(URL_MESSAGES)
                        .with(jwt()
                                .jwt(b -> b.subject(USER_ID).claim(CLAIM_ORG_ID, ORG_ID))
                                .authorities(role(ROLE_PUBLISH)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("POST /messages - ROLE_SERVICE로도 발행 가능 (202)")
    void publish_withServiceRole_returns202() throws Exception {
        PublishRequest request = new PublishRequest(PROGRAM_ID, CHANNEL_ID, "SYSTEM",
                Map.of("key", "val"), 30);
        AcceptResponse response = new AcceptResponse(UUID.randomUUID().toString(), "trace-svc");

        given(ingestService.accept(any(), any(), any(), any(), any())).willReturn(response);

        mockMvc.perform(post(URL_MESSAGES)
                        .with(jwt()
                                .jwt(b -> b.subject("svc-account").claim(CLAIM_ORG_ID, ORG_ID))
                                .authorities(role("SERVICE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());
    }

    private SimpleGrantedAuthority role(String role) {
        return new SimpleGrantedAuthority("ROLE_" + role);
    }
}
