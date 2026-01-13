package com.soundbar91.infrastructure.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA 설정
 */
@Configuration
@EntityScan(basePackages = "com.soundbar91.domain")
@EnableJpaRepositories(basePackages = "com.soundbar91.infrastructure")
public class JpaConfig {
}
