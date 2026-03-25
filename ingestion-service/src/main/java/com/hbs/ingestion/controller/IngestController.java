package com.hbs.ingestion.controller;

import com.hbs.ingestion.dto.ApiResponse;
import com.hbs.ingestion.dto.request.PublishRequest;
import com.hbs.ingestion.dto.response.AcceptResponse;
import com.hbs.ingestion.service.IngestService;
import com.hbs.security.jwt.SecurityContextHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class IngestController {

    private final IngestService ingestService;

    /**
     * 메시지 발행 요청을 수신하고 비동기 처리를 위해 즉시 202 Accepted를 반환한다.
     * ROLE_PUBLISH 또는 ROLE_SERVICE 권한이 필요하다.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('PUBLISH', 'SERVICE')")
    public ResponseEntity<ApiResponse<AcceptResponse>> publish(
            @Valid @RequestBody PublishRequest request) {

        String orgId = SecurityContextHelper.getOrgId();
        String userId = SecurityContextHelper.getUserId();
        String traceId = SecurityContextHelper.getTraceId();

        AcceptResponse response = ingestService.accept(orgId, userId, "USER", request, traceId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.ok(response));
    }
}
