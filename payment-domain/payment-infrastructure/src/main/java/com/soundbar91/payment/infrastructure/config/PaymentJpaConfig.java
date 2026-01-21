package com.soundbar91.payment.infrastructure.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Payment 도메인 JPA 설정
 */
@Configuration
@EntityScan(basePackages = "com.soundbar91.payment.domain.entity")
@EnableJpaRepositories(basePackages = "com.soundbar91.payment.infrastructure.repository")
public class PaymentJpaConfig {
}
