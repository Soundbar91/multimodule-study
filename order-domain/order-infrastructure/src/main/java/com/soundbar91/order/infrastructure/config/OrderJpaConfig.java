package com.soundbar91.order.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Order 도메인 JPA 설정
 */
@Configuration
@EntityScan(basePackages = "com.soundbar91.order.domain.entity")
@EnableJpaRepositories(basePackages = "com.soundbar91.order.infrastructure.repository")
public class OrderJpaConfig {
}
