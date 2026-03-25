package com.hbs.tracking;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@ComponentScan(basePackages = "com.hbs.tracking")
@EnableJpaRepositories(basePackages = "com.hbs.tracking.repository")
@EntityScan(basePackages = "com.hbs.tracking.entity")
public class TrackingAutoConfiguration {
}
