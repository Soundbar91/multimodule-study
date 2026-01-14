package com.soundbar91.user.infrastructure.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * User 도메인 JPA 설정
 */
@Configuration
@EntityScan(basePackages = "com.soundbar91.user.domain.entity")
@EnableJpaRepositories(basePackages = "com.soundbar91.user.infrastructure.repository")
public class UserJpaConfig {
}
