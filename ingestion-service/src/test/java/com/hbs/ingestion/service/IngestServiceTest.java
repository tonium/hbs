package com.hbs.ingestion.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hbs.ingestion.dto.request.PublishRequest;
import com.hbs.ingestion.dto.response.AcceptResponse;
import com.hbs.ingestion.entity.IngestJob;
import com.hbs.ingestion.exception.PublishPermissionDeniedException;
import com.hbs.ingestion.repository.IngestJobRepository;
import com.hbs.tracking.service.TrackingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SuppressWarnings("java:S100")
@ExtendWith(MockitoExtension.class)
class IngestServiceTest {

    private static final String ORG_ID = "org1";
    private static final String USER_ID = "user1";
    private static final String PROGRAM_ID = "prog1";
    private static final String CHANNEL_ID = "ch1";
    private static final String TRACE_ID = "trace-abc";

    @Mock
    private IngestJobRepository jobRepository;

    @Mock
    private PublisherAclService aclService;

    @Mock
    private TrackingService trackingService;

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private IngestService ingestService;

    @Test
    void accept_success_returns_202_response() {
        PublishRequest request = new PublishRequest(PROGRAM_ID, CHANNEL_ID, "ALERT",
                Map.of("key", "value"), 60);

        doNothing().when(aclService).assertPermission(anyString(), anyString(), anyString(),
                anyString(), anyString());
        when(jobRepository.save(any(IngestJob.class))).thenAnswer(inv -> inv.getArgument(0));

        AcceptResponse response = ingestService.accept(ORG_ID, USER_ID, "USER", request, TRACE_ID);

        assertThat(response.jobId()).isNotNull();
        assertThat(response.traceId()).isEqualTo(TRACE_ID);
        verify(jobRepository).save(any(IngestJob.class));
        verify(trackingService).recordEvent(any());
    }

    @Test
    void accept_saves_job_with_correct_fields() {
        PublishRequest request = new PublishRequest(PROGRAM_ID, CHANNEL_ID, "NOTICE",
                Map.of("msg", "hello"), 30);

        doNothing().when(aclService).assertPermission(any(), any(), any(), any(), any());
        when(jobRepository.save(any(IngestJob.class))).thenAnswer(inv -> inv.getArgument(0));

        ingestService.accept(ORG_ID, USER_ID, "USER", request, TRACE_ID);

        ArgumentCaptor<IngestJob> captor = ArgumentCaptor.forClass(IngestJob.class);
        verify(jobRepository).save(captor.capture());

        IngestJob saved = captor.getValue();
        assertThat(saved.getOrgId()).isEqualTo(ORG_ID);
        assertThat(saved.getProgramId()).isEqualTo(PROGRAM_ID);
        assertThat(saved.getChannelId()).isEqualTo(CHANNEL_ID);
        assertThat(saved.getType()).isEqualTo("NOTICE");
        assertThat(saved.getTraceId()).isEqualTo(TRACE_ID);
    }

    @Test
    void accept_throws_when_no_permission() {
        PublishRequest request = new PublishRequest(PROGRAM_ID, CHANNEL_ID, "ALERT",
                Map.of(), 60);

        doThrow(new PublishPermissionDeniedException(USER_ID, PROGRAM_ID, CHANNEL_ID))
                .when(aclService).assertPermission(any(), any(), any(), any(), any());

        assertThatThrownBy(() -> ingestService.accept(ORG_ID, USER_ID, "USER", request, TRACE_ID))
                .isInstanceOf(PublishPermissionDeniedException.class);

        verify(jobRepository, never()).save(any());
    }

    @Test
    void accept_with_null_channelId_saves_job() {
        PublishRequest request = new PublishRequest(PROGRAM_ID, null, "BROADCAST",
                Map.of("data", "x"), 120);

        doNothing().when(aclService).assertPermission(any(), any(), any(), any(), isNull());
        when(jobRepository.save(any(IngestJob.class))).thenAnswer(inv -> inv.getArgument(0));

        AcceptResponse response = ingestService.accept(ORG_ID, USER_ID, "USER", request, TRACE_ID);

        assertThat(response.jobId()).isNotNull();
    }
}
