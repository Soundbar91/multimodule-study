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

# Phase 5.2: 보안 및 인증 모듈

## 학습 목표
- 공통 보안 설정 모듈화
- JWT 인증 모듈 분리
- Spring Security 설정 공유
- 도메인 간 보안 모듈 재사용

---

## 1. security-common 모듈 구조

### 1.1 디렉토리 구조
```
security-common/
├── build.gradle
└── src/main/
    ├── java/com/soundbar91/security/
    │   ├── config/
    │   │   ├── SecurityAutoConfiguration.java    # 자동 구성
    │   │   └── SecurityConfig.java               # Security 설정
    │   ├── jwt/
    │   │   ├── JwtProperties.java                # JWT 설정 속성
    │   │   ├── JwtTokenProvider.java             # 토큰 생성/검증
    │   │   ├── JwtAuthenticationFilter.java      # 인증 필터
    │   │   ├── JwtAuthenticationEntryPoint.java  # 인증 실패 처리
    │   │   ├── JwtAccessDeniedHandler.java       # 권한 거부 처리
    │   │   └── TokenResponse.java                # 토큰 응답 DTO
    │   ├── exception/
    │   │   ├── AuthErrorCode.java                # 인증 에러 코드
    │   │   └── AuthenticationException.java      # 인증 예외
    │   └── util/
    │       └── SecurityUtils.java                # 보안 유틸리티
    └── resources/
        └── META-INF/spring/
            └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

### 1.2 build.gradle 설정
```groovy
// security-common/build.gradle
dependencies {
    implementation project(':common')
    implementation project(':config')

    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'com.fasterxml.jackson.core:jackson-databind'

    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'

    // Configuration Processor
    annotationProcessor 'org.springframework.boot:spring-boot-configuration-processor'
}

jar {
    enabled = true
}
```

---

## 2. JWT 토큰 관리

### 2.1 JWT 설정 속성
```java
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        long accessTokenExpiration,
        long refreshTokenExpiration,
        String issuer
) {
    public JwtProperties {
        if (secret == null || secret.isBlank()) {
            secret = "default-secret-key-for-development-only";
        }
        if (accessTokenExpiration <= 0) {
            accessTokenExpiration = 3600000L; // 1시간
        }
        if (refreshTokenExpiration <= 0) {
            refreshTokenExpiration = 604800000L; // 7일
        }
        if (issuer == null || issuer.isBlank()) {
            issuer = "nuga";
        }
    }
}
```

### 2.2 application.yml JWT 설정
```yaml
# JWT 설정
jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-for-development}
  access-token-expiration: ${JWT_ACCESS_EXPIRATION:3600000}  # 1시간
  refresh-token-expiration: ${JWT_REFRESH_EXPIRATION:604800000}  # 7일
  issuer: ${JWT_ISSUER:nuga}
```

### 2.3 JwtTokenProvider
```java
public class JwtTokenProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private final SecretKey secretKey;
    private final JwtProperties jwtProperties;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(
            jwtProperties.secret().getBytes(StandardCharsets.UTF_8)
        );
    }

    // Access Token 생성
    public String createAccessToken(Authentication authentication) {
        return createToken(authentication, jwtProperties.accessTokenExpiration());
    }

    // Refresh Token 생성
    public String createRefreshToken(Authentication authentication) {
        return createToken(authentication, jwtProperties.refreshTokenExpiration());
    }

    // 토큰에서 인증 정보 추출
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        Collection<? extends GrantedAuthority> authorities =
            Arrays.stream(claims.get(AUTHORITIES_KEY, String.class).split(","))
                .filter(auth -> !auth.isBlank())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        User principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

---

## 3. Spring Security 설정

### 3.1 SecurityConfig
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    @ConditionalOnMissingBean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/**",
                    "/api/public/**",
                    "/actuator/health",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 3.2 JWT 인증 필터
```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
```

---

## 4. 예외 처리

### 4.1 인증 에러 코드
```java
public enum AuthErrorCode {

    // 인증 관련
    INVALID_TOKEN("AUTH001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN("AUTH002", "만료된 토큰입니다."),
    UNSUPPORTED_TOKEN("AUTH003", "지원하지 않는 토큰 형식입니다."),
    EMPTY_TOKEN("AUTH004", "토큰이 비어있습니다."),

    // 인가 관련
    ACCESS_DENIED("AUTH005", "접근 권한이 없습니다."),
    INSUFFICIENT_AUTHORITY("AUTH006", "권한이 부족합니다."),

    // 로그인 관련
    INVALID_CREDENTIALS("AUTH007", "아이디 또는 비밀번호가 올바르지 않습니다."),
    ACCOUNT_DISABLED("AUTH008", "비활성화된 계정입니다."),
    ACCOUNT_LOCKED("AUTH009", "잠긴 계정입니다.");

    private final String code;
    private final String message;
    // ...
}
```

### 4.2 인증 예외 응답 (401)
```java
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> errorResponse = Map.of(
            "timestamp", LocalDateTime.now().toString(),
            "status", 401,
            "error", "Unauthorized",
            "message", "인증이 필요합니다.",
            "path", request.getRequestURI()
        );

        new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
    }
}
```

---

## 5. 보안 유틸리티

### 5.1 SecurityUtils
```java
public final class SecurityUtils {

    // 현재 인증된 사용자명 조회
    public static Optional<String> getCurrentUsername() {
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return Optional.of(userDetails.getUsername());
        }
        return Optional.empty();
    }

    // 인증 여부 확인
    public static boolean isAuthenticated() {
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
            && authentication.isAuthenticated()
            && !"anonymousUser".equals(authentication.getPrincipal());
    }

    // 특정 역할 보유 여부 확인
    public static boolean hasRole(String role) {
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;

        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals(roleWithPrefix));
    }
}
```

---

## 6. 도메인 모듈 연동

### 6.1 의존성 추가
```groovy
// api/build.gradle
dependencies {
    implementation project(':security-common')
    // ...
}
```

### 6.2 settings.gradle 등록
```groovy
// settings.gradle
include 'security-common'
```

### 6.3 Auto Configuration
security-common 모듈은 Auto Configuration을 통해 자동으로 설정됩니다.

```
// META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
com.soundbar91.security.config.SecurityAutoConfiguration
```

### 6.4 커스텀 Security 설정 (선택)
필요한 경우 도메인별로 Security 설정을 오버라이드할 수 있습니다.

```java
@Configuration
public class CustomSecurityConfig {

    @Bean
    public SecurityFilterChain customSecurityFilterChain(HttpSecurity http) {
        // 커스텀 설정...
    }
}
```

---

## 7. 사용 예시

### 7.1 로그인 API 구현 예시
```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.username(),
                request.password()
            )
        );

        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        return new TokenResponse(accessToken, refreshToken, 3600000L);
    }
}
```

### 7.2 권한 기반 접근 제어
```java
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        // ADMIN 역할만 접근 가능
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public void deleteUser(@PathVariable Long id) {
        // USER_DELETE 권한만 접근 가능
    }
}
```

### 7.3 현재 사용자 정보 활용
```java
@Service
@RequiredArgsConstructor
public class OrderService {

    public Order createOrder(OrderRequest request) {
        String username = SecurityUtils.getCurrentUsernameOrThrow();
        // 현재 로그인한 사용자로 주문 생성
    }
}
```

---

## 8. 체크리스트

### 8.1 모듈 구성
- [x] security-common 모듈 생성
- [x] settings.gradle에 모듈 등록
- [x] build.gradle 의존성 설정

### 8.2 JWT 구현
- [x] JwtProperties 설정 클래스
- [x] JwtTokenProvider 토큰 생성/검증
- [x] JwtAuthenticationFilter 인증 필터
- [x] TokenResponse DTO

### 8.3 Security 설정
- [x] SecurityConfig 기본 설정
- [x] CORS 설정
- [x] 세션 정책 (STATELESS)
- [x] PasswordEncoder (BCrypt)

### 8.4 예외 처리
- [x] JwtAuthenticationEntryPoint (401)
- [x] JwtAccessDeniedHandler (403)
- [x] AuthErrorCode enum
- [x] AuthenticationException

### 8.5 유틸리티
- [x] SecurityUtils 구현
- [x] 현재 사용자 조회
- [x] 권한 확인 메서드

### 8.6 연동
- [x] api 모듈에 의존성 추가
- [x] application.yml JWT 설정
- [x] Auto Configuration 등록

---

## 9. 주의사항

### 9.1 JWT Secret 관리
- 프로덕션에서는 반드시 환경 변수로 주입
- 최소 256비트 (32자) 이상의 시크릿 키 사용
- 절대 소스 코드에 실제 시크릿 키 커밋 금지

### 9.2 토큰 만료 시간
| 토큰 타입 | 권장 만료 시간 | 용도 |
|----------|---------------|------|
| Access Token | 15분 ~ 1시간 | API 인증 |
| Refresh Token | 7일 ~ 30일 | 토큰 갱신 |

### 9.3 보안 권장사항
- HTTPS 필수 사용
- Refresh Token은 HttpOnly 쿠키로 전달 권장
- Token Blacklist 구현 고려 (로그아웃 시)
- Rate Limiting 적용

---

## 10. 다음 단계

Phase 5.2 완료 후 다음 학습 방향:
1. **Phase 5.3**: 테스트 전략 (test-common 모듈)
2. **Phase 5.4**: 모니터링 및 로깅 (monitoring-common)
