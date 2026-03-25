package com.hbs.security;

import com.hbs.security.config.SecurityConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(SecurityConfig.class)
public class SecurityAutoConfiguration {
}
