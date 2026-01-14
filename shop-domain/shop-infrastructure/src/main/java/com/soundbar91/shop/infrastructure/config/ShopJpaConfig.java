package com.soundbar91.shop.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Shop 도메인 JPA 설정
 */
@Configuration
@EntityScan(basePackages = "com.soundbar91.shop.domain.entity")
@EnableJpaRepositories(basePackages = "com.soundbar91.shop.infrastructure.repository")
public class ShopJpaConfig {
}
