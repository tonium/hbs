package com.hbs.subscription.controller;

import com.hbs.security.jwt.SecurityContextHelper;
import com.hbs.subscription.dto.ApiResponse;
import com.hbs.subscription.dto.request.SubscribeRequest;
import com.hbs.subscription.dto.response.SubscribeResponse;
import com.hbs.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @PreAuthorize("hasRole('SUBSCRIBE')")
    public ResponseEntity<ApiResponse<SubscribeResponse>> subscribe(
            @Valid @RequestBody SubscribeRequest request) {

        String userId = SecurityContextHelper.getUserId();
        String orgId = SecurityContextHelper.getOrgId();
        String traceId = extractTraceId();

        SubscribeResponse response = subscriptionService.subscribe(orgId, userId, traceId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUBSCRIBE')")
    public ResponseEntity<ApiResponse<Void>> unsubscribe(@PathVariable Long id) {
        String userId = SecurityContextHelper.getUserId();
        String orgId = SecurityContextHelper.getOrgId();

        subscriptionService.unsubscribe(id, userId, orgId, extractTraceId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PatchMapping("/{id}/pause")
    @PreAuthorize("hasRole('SUBSCRIBE')")
    public ResponseEntity<ApiResponse<SubscribeResponse>> pause(@PathVariable Long id) {
        String userId = SecurityContextHelper.getUserId();
        String orgId = SecurityContextHelper.getOrgId();

        SubscribeResponse response = subscriptionService.pause(id, userId, orgId, extractTraceId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}/resume")
    @PreAuthorize("hasRole('SUBSCRIBE')")
    public ResponseEntity<ApiResponse<SubscribeResponse>> resume(@PathVariable Long id) {
        String userId = SecurityContextHelper.getUserId();
        String orgId = SecurityContextHelper.getOrgId();

        SubscribeResponse response = subscriptionService.resume(id, userId, orgId, extractTraceId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUBSCRIBE')")
    public ResponseEntity<ApiResponse<List<SubscribeResponse>>> list() {
        String userId = SecurityContextHelper.getUserId();
        String orgId = SecurityContextHelper.getOrgId();

        List<SubscribeResponse> responses = subscriptionService.listByUser(orgId, userId);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    private String extractTraceId() {
        try {
            return SecurityContextHelper.getTraceId();
        } catch (Exception e) {
            return java.util.UUID.randomUUID().toString();
        }
    }
}
