package com.hbs.security.jwt;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * SecurityContext에서 JWT 클레임을 추출하는 유틸리티 클래스.
 * 서비스 레이어에서 현재 요청의 userId, orgId를 조회할 때 사용한다.
 */
public class SecurityContextHelper {

    private SecurityContextHelper() {}

    public static String getUserId() {
        return getJwt().getSubject();
    }

    public static String getOrgId() {
        return getJwt().getClaimAsString("orgId");
    }

    public static String getTraceId() {
        return getJwt().getClaimAsString("traceId");
    }

    public static Jwt getJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken();
        }
        throw new IllegalStateException("JWT 인증 토큰을 찾을 수 없습니다.");
    }
}
