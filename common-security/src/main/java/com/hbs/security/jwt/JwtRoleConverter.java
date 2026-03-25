package com.hbs.security.jwt;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Keycloak JWT의 realm_access.roles 클레임을 Spring Security GrantedAuthority로 변환한다.
 * 역할 이름은 ROLE_ 접두사를 붙여 대문자로 변환한다.
 * 예: "ROLE_PUBLISH" -> SimpleGrantedAuthority("ROLE_PUBLISH")
 */
public class JwtRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null || !realmAccess.containsKey("roles")) {
            return Collections.emptyList();
        }

        List<String> roles = (List<String>) realmAccess.get("roles");
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(toRoleName(role)))
                .collect(Collectors.toList());
    }

    private String toRoleName(String role) {
        String upper = role.toUpperCase();
        return upper.startsWith("ROLE_") ? upper : "ROLE_" + upper;
    }
}
