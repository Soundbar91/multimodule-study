# Phase 5.1: 설정 관리 모듈

## 학습 목표
- 환경별 설정 관리 방법 이해 (dev, stage, prod)
- 외부 설정 주입 방법 학습
- 공통 로깅 설정 모듈화
- ConfigurationProperties를 활용한 타입 안전한 설정 바인딩

---

## 1. config 모듈 구조

### 1.1 디렉토리 구조
```
config/
├── build.gradle
└── src/main/
    ├── java/com/soundbar91/config/
    │   ├── CommonConfig.java                    # 공통 설정 Configuration
    │   └── properties/
    │       ├── AppProperties.java               # 애플리케이션 속성
    │       └── DatabaseProperties.java          # 데이터베이스 속성
    └── resources/
        ├── META-INF/
        │   └── spring/
        │       └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
        ├── application.yml                      # 공통 설정
        ├── application-dev.yml                  # 개발 환경
        ├── application-stage.yml                # 스테이징 환경
        ├── application-prod.yml                 # 프로덕션 환경
        └── logback-spring.xml                   # 로깅 설정
```

### 1.2 build.gradle 설정
```groovy
// config/build.gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter'

    // Configuration Processor for @ConfigurationProperties
    annotationProcessor 'org.springframework.boot:spring-boot-configuration-processor'
}

// Library module: produces jar only
jar {
    enabled = true
}
```

---

## 2. 환경별 설정 파일

### 2.1 공통 설정 (application.yml)
모든 환경에서 공유되는 기본 설정입니다.

```yaml
# 공통 설정 (모든 환경에서 적용)
spring:
  application:
    name: nuga
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  jpa:
    open-in-view: false
    properties:
      hibernate:
        default_batch_fetch_size: 100

server:
  shutdown: graceful

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized

# 애플리케이션 커스텀 속성
app:
  name: ${spring.application.name}
  version: ${APP_VERSION:0.0.1}
  api:
    base-url: /api/v1
    timeout: 30000
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:*}
    allowed-methods: GET,POST,PUT,DELETE,OPTIONS
    allowed-headers: "*"
    allow-credentials: true
    max-age: 3600

# 데이터베이스 커스텀 속성
database:
  schema: ${DB_SCHEMA:public}
  migration:
    enabled: ${DB_MIGRATION_ENABLED:false}
    locations: classpath:db/migration
    validate-on-migrate: true
```

### 2.2 개발 환경 (application-dev.yml)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:devdb
    driver-class-name: org.h2.Driver
    username: sa
    password:

  h2:
    console:
      enabled: true
      path: /h2-console

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true

server:
  port: 8080

logging:
  level:
    root: INFO
    com.soundbar91: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.orm.jdbc.bind: TRACE
```

### 2.3 스테이징 환경 (application-stage.yml)
```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/nuga_stage}
    driver-class-name: org.postgresql.Driver
    username: ${DB_USERNAME:nuga}
    password: ${DB_PASSWORD:nuga_stage_password}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5

  h2:
    console:
      enabled: false

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

logging:
  level:
    root: INFO
    com.soundbar91: INFO
    org.hibernate.SQL: WARN
```

### 2.4 프로덕션 환경 (application-prod.yml)
```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/nuga_prod}
    driver-class-name: org.postgresql.Driver
    username: ${DB_USERNAME:nuga}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
      pool-name: NugaHikariPool

  h2:
    console:
      enabled: false

  jpa:
    hibernate:
      ddl-auto: none
    show-sql: false
    properties:
      hibernate:
        jdbc:
          batch_size: 50
        order_inserts: true
        order_updates: true

server:
  port: ${SERVER_PORT:8080}
  tomcat:
    threads:
      max: 200
      min-spare: 10

logging:
  level:
    root: WARN
    com.soundbar91: INFO

management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: never
```

---

## 3. 외부 설정 주입

### 3.1 환경 변수를 통한 설정 주입
```bash
# 프로파일 설정
export SPRING_PROFILES_ACTIVE=prod

# 데이터베이스 설정
export DB_URL=jdbc:postgresql://db.example.com:5432/nuga
export DB_USERNAME=nuga_user
export DB_PASSWORD=secure_password

# 애플리케이션 설정
export APP_VERSION=1.0.0
export SERVER_PORT=8080
export CORS_ALLOWED_ORIGINS=https://example.com
```

### 3.2 JVM 옵션을 통한 설정 주입
```bash
java -jar api.jar \
  -Dspring.profiles.active=prod \
  -Ddb.url=jdbc:postgresql://localhost:5432/nuga \
  -Ddb.username=nuga \
  -Ddb.password=password
```

### 3.3 ConfigurationProperties 활용
타입 안전한 설정 바인딩을 위해 Record 클래스를 사용합니다.

```java
// AppProperties.java
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
    ) {}

    public record Cors(
            String[] allowedOrigins,
            String[] allowedMethods,
            String[] allowedHeaders,
            boolean allowCredentials,
            long maxAge
    ) {}
}
```

### 3.4 설정 사용 예시
```java
@RestController
@RequiredArgsConstructor
public class InfoController {

    private final AppProperties appProperties;

    @GetMapping("/info")
    public Map<String, Object> getInfo() {
        return Map.of(
            "name", appProperties.name(),
            "version", appProperties.version(),
            "apiBaseUrl", appProperties.api().baseUrl()
        );
    }
}
```

---

## 4. Logback 설정

### 4.1 환경별 로깅 전략

| 환경 | 콘솔 | 파일 | 에러 파일 | JSON |
|------|------|------|-----------|------|
| dev | O | X | X | X |
| stage | O | O | O | X |
| prod | X | O | O | O |

### 4.2 logback-spring.xml 구성
```xml
<configuration scan="true" scanPeriod="30 seconds">

    <!-- 공통 속성 정의 -->
    <property name="LOG_PATH" value="${LOG_PATH:-logs}"/>
    <property name="LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"/>

    <!-- 콘솔 Appender -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
        </encoder>
    </appender>

    <!-- 파일 Appender (Rolling) -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/nuga.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/nuga.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxHistory>30</maxHistory>
            <totalSizeCap>3GB</totalSizeCap>
        </rollingPolicy>
    </appender>

    <!-- 개발 환경 -->
    <springProfile name="dev">
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
        <logger name="com.soundbar91" level="DEBUG"/>
    </springProfile>

    <!-- 프로덕션 환경 -->
    <springProfile name="prod">
        <root level="WARN">
            <appender-ref ref="FILE"/>
        </root>
        <logger name="com.soundbar91" level="INFO"/>
    </springProfile>

</configuration>
```

### 4.3 로그 레벨 가이드

| 레벨 | 용도 | 예시 |
|------|------|------|
| ERROR | 심각한 오류, 즉시 대응 필요 | DB 연결 실패, 외부 API 장애 |
| WARN | 주의가 필요한 상황 | 재시도 발생, 성능 저하 |
| INFO | 중요한 비즈니스 이벤트 | 주문 생성, 결제 완료 |
| DEBUG | 개발/디버깅용 상세 정보 | 메서드 호출, 파라미터 값 |
| TRACE | 매우 상세한 디버깅 정보 | SQL 바인딩, 객체 상태 |

---

## 5. config 모듈 연동

### 5.1 다른 모듈에서 의존성 추가
```groovy
// api/build.gradle
dependencies {
    implementation project(':config')
    // ...
}
```

### 5.2 Auto Configuration 등록
```
// META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
com.soundbar91.config.CommonConfig
```

### 5.3 모듈별 설정 오버라이드
각 모듈에서 필요한 경우 설정을 오버라이드할 수 있습니다.

```properties
# api/src/main/resources/application.properties
spring.application.name=nuga-api
spring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}
```

---

## 6. 빌드 및 실행

### 6.1 빌드 명령어
```bash
# config 모듈 빌드
./gradlew :config:build

# 전체 빌드
./gradlew build
```

### 6.2 프로파일별 실행
```bash
# 개발 환경
./gradlew :api:bootRun

# 스테이징 환경
./gradlew :api:bootRun --args='--spring.profiles.active=stage'

# 프로덕션 환경
java -jar api.jar --spring.profiles.active=prod
```

### 6.3 Docker 환경에서 실행
```dockerfile
FROM eclipse-temurin:21-jre

COPY api.jar /app/api.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV DB_URL=jdbc:postgresql://db:5432/nuga
ENV DB_USERNAME=nuga
ENV DB_PASSWORD=password

ENTRYPOINT ["java", "-jar", "/app/api.jar"]
```

---

## 7. 체크리스트

### 7.1 설정 파일
- [x] application.yml (공통 설정) 작성
- [x] application-dev.yml (개발 환경) 작성
- [x] application-stage.yml (스테이징 환경) 작성
- [x] application-prod.yml (프로덕션 환경) 작성

### 7.2 로깅 설정
- [x] logback-spring.xml 작성
- [x] 환경별 로깅 레벨 설정
- [x] Rolling 파일 정책 설정

### 7.3 ConfigurationProperties
- [x] AppProperties 클래스 작성
- [x] DatabaseProperties 클래스 작성
- [x] CommonConfig에서 활성화

### 7.4 모듈 연동
- [x] settings.gradle에 config 모듈 등록
- [x] api 모듈에서 config 의존성 추가
- [x] Auto Configuration 등록

### 7.5 빌드 검증
- [x] config 모듈 빌드 성공
- [x] api 모듈 빌드 성공
- [x] 애플리케이션 테스트 통과

---

## 8. 주의사항

### 8.1 민감 정보 관리
- 비밀번호, API 키 등은 환경 변수로 주입
- application.yml에 민감 정보 직접 작성 금지
- .gitignore에 로컬 설정 파일 추가 고려

### 8.2 프로파일 기본값
- 프로파일을 지정하지 않으면 `dev`가 기본값
- 프로덕션 배포 시 반드시 프로파일 명시

### 8.3 설정 우선순위 (높은 순)
1. 커맨드라인 인자 (`--spring.datasource.url=...`)
2. 환경 변수 (`SPRING_DATASOURCE_URL`)
3. application-{profile}.yml
4. application.yml
5. @ConfigurationProperties 기본값

---

## 9. 다음 단계

Phase 5.1 완료 후 다음 학습 방향:
1. **Phase 5.2**: 보안 및 인증 모듈 (security-common)
2. **Phase 5.3**: 테스트 전략 (test-common 모듈)
3. **Phase 5.4**: 모니터링 및 로깅 (monitoring-common)
