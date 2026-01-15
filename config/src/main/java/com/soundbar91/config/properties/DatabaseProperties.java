package com.soundbar91.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 데이터베이스 관련 추가 설정 속성
 * Spring Boot의 기본 datasource 설정 외에 추가적인 커스텀 설정을 관리합니다.
 *
 * 사용 예시:
 * database:
 *   schema: public
 *   migration:
 *     enabled: true
 *     locations: classpath:db/migration
 */
@ConfigurationProperties(prefix = "database")
public record DatabaseProperties(
        String schema,
        Migration migration
) {

    public record Migration(
            boolean enabled,
            String locations,
            boolean validateOnMigrate
    ) {
        public Migration {
            if (locations == null) {
                locations = "classpath:db/migration";
            }
        }
    }

    public DatabaseProperties {
        if (schema == null) {
            schema = "public";
        }
        if (migration == null) {
            migration = new Migration(false, null, true);
        }
    }
}
