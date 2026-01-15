package com.soundbar91.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 애플리케이션 공통 설정 속성
 * application.yml에서 'app' prefix로 시작하는 설정을 바인딩합니다.
 *
 * 사용 예시:
 * app:
 *   name: nuga
 *   version: 1.0.0
 *   api:
 *     base-url: /api/v1
 *   cors:
 *     allowed-origins: http://localhost:3000
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String name,
        String version,
        Api api,
        Cors cors
) {

    public record Api(
            String baseUrl,
            int timeout
    ) {
        public Api {
            if (baseUrl == null) {
                baseUrl = "/api/v1";
            }
            if (timeout <= 0) {
                timeout = 30000;
            }
        }
    }

    public record Cors(
            String[] allowedOrigins,
            String[] allowedMethods,
            String[] allowedHeaders,
            boolean allowCredentials,
            long maxAge
    ) {
        public Cors {
            if (allowedOrigins == null) {
                allowedOrigins = new String[]{"*"};
            }
            if (allowedMethods == null) {
                allowedMethods = new String[]{"GET", "POST", "PUT", "DELETE", "OPTIONS"};
            }
            if (allowedHeaders == null) {
                allowedHeaders = new String[]{"*"};
            }
            if (maxAge <= 0) {
                maxAge = 3600;
            }
        }
    }

    public AppProperties {
        if (name == null) {
            name = "nuga";
        }
        if (version == null) {
            version = "0.0.1";
        }
        if (api == null) {
            api = new Api(null, 0);
        }
        if (cors == null) {
            cors = new Cors(null, null, null, true, 0);
        }
    }
}
