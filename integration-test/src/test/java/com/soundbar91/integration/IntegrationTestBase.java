package com.soundbar91.integration;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration Test Base Class
 * All integration tests should extend this class for common configuration.
 */
@SpringBootTest(classes = IntegrationTestBase.IntegrationTestConfig.class)
@ActiveProfiles("test")
@Transactional
public abstract class IntegrationTestBase {

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = {
            "com.soundbar91.user",
            "com.soundbar91.shop",
            "com.soundbar91.order",
            "com.soundbar91.payment"
    })
    @EntityScan(basePackages = {
            "com.soundbar91.user.domain.entity",
            "com.soundbar91.shop.domain.entity",
            "com.soundbar91.order.domain.entity",
            "com.soundbar91.payment.domain.entity"
    })
    @EnableJpaRepositories(basePackages = {
            "com.soundbar91.user.infrastructure.repository",
            "com.soundbar91.shop.infrastructure.repository",
            "com.soundbar91.order.infrastructure.repository",
            "com.soundbar91.payment.infrastructure.repository"
    })
    static class IntegrationTestConfig {
    }
}
