# Phase 3: 도메인 중심 멀티 모듈 (Domain-Driven Design)

## 학습 목표
- DDD의 Bounded Context 개념 이해
- 도메인별 독립 모듈 구성
- 도메인 간 통신 방법 (직접 호출 vs 이벤트 기반)

---

## 1. 도메인 중심 아키텍처란?

### 1.1 DDD의 Bounded Context
**Bounded Context**는 특정 도메인 모델이 적용되는 명확한 경계를 의미합니다.

```
┌─────────────────────────────────────────────────────────────┐
│                     E-Commerce System                        │
├───────────────────┬───────────────────┬─────────────────────┤
│   User Context    │   Shop Context    │   Order Context     │
│                   │                   │                     │
│  - 회원 가입      │  - 상점 등록      │  - 주문 생성        │
│  - 프로필 관리    │  - 상점 정보 수정 │  - 주문 상태 관리   │
│  - 권한 관리      │  - 카테고리 관리  │  - 배송 추적        │
└───────────────────┴───────────────────┴─────────────────────┘
```

### 1.2 Phase 2와의 차이점

| 구분 | Phase 2 (계층형) | Phase 3 (도메인 중심) |
|------|-----------------|---------------------|
| 분리 기준 | 기술 계층 | 비즈니스 도메인 |
| 모듈 구조 | api, domain, infrastructure | user-domain, shop-domain, order-domain |
| 의존성 방향 | 상위 → 하위 | 도메인 간 직접/이벤트 기반 |
| 확장성 | 수직 확장 | 수평 확장 (마이크로서비스 전환 용이) |

---

## 2. 프로젝트 구조

### 2.1 전체 모듈 구조
```
multi-module-study/
├── common/                         # 공통 모듈 (Phase 2 유지)
├── domain/                         # 계층형 도메인 (Phase 2 유지)
├── infrastructure/                 # 계층형 인프라 (Phase 2 유지)
├── api/                           # 계층형 API (Phase 2 유지)
│
├── user-domain/                   # 사용자 도메인 (Phase 3)
│   ├── user-api/                  # REST API 계층
│   ├── user-service/              # 도메인 로직 계층
│   └── user-infrastructure/       # 영속성 계층
│
├── shop-domain/                   # 상점 도메인 (Phase 3)
│   ├── shop-api/
│   ├── shop-service/
│   └── shop-infrastructure/
│
└── order-domain/                  # 주문 도메인 (Phase 3)
    ├── order-api/
    ├── order-service/
    └── order-infrastructure/
```

### 2.2 도메인 내부 구조
```
user-domain/
├── build.gradle                   # 컨테이너 모듈 설정
│
├── user-service/                  # 핵심 도메인 로직
│   ├── build.gradle
│   └── src/main/java/com/soundbar91/user/
│       ├── domain/
│       │   ├── entity/User.java       # 도메인 엔티티
│       │   ├── vo/UserRole.java       # 값 객체
│       │   ├── repository/            # 레포지토리 인터페이스
│       │   └── event/                 # 도메인 이벤트
│       └── service/UserService.java   # 도메인 서비스
│
├── user-infrastructure/           # 영속성 구현
│   ├── build.gradle
│   └── src/main/java/com/soundbar91/user/infrastructure/
│       ├── repository/
│       │   ├── UserJpaRepository.java
│       │   └── UserRepositoryImpl.java
│       └── config/UserJpaConfig.java
│
└── user-api/                      # REST API
    ├── build.gradle
    └── src/main/java/com/soundbar91/user/api/
        ├── controller/UserController.java
        └── dto/
            ├── request/
            └── response/
```

---

## 3. 모듈 간 의존성

### 3.1 의존성 다이어그램
```
                    ┌─────────┐
                    │ common  │
                    └────┬────┘
                         │
         ┌───────────────┼───────────────┐
         │               │               │
         ▼               ▼               ▼
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│ user-service│  │ shop-service│  │order-service│
└──────┬──────┘  └──────┬──────┘  └──────┬──────┘
       │                │                │
       │    ┌───────────┴───────────┐    │
       │    │                       │    │
       ▼    ▼                       ▼    ▼
┌─────────────────┐           ┌─────────────────┐
│ order-service   │◄──────────│ shop-service    │
│ (직접 의존)     │ 이벤트구독│ (이벤트 구독)   │
└─────────────────┘           └─────────────────┘
```

### 3.2 build.gradle 의존성 설정

**order-service/build.gradle** (도메인 간 직접 의존)
```groovy
dependencies {
    implementation project(':common')
    // 도메인 간 직접 의존: 사용자/상점 검증을 위해
    implementation project(':user-domain:user-service')
    implementation project(':shop-domain:shop-service')

    implementation 'org.springframework.boot:spring-boot-starter'
    implementation 'org.springframework:spring-tx'
    implementation 'org.springframework:spring-context'
}
```

**shop-service/build.gradle** (이벤트 구독을 위한 의존)
```groovy
dependencies {
    implementation project(':common')
    // 이벤트 구독을 위한 User 도메인 의존
    implementation project(':user-domain:user-service')

    implementation 'org.springframework.boot:spring-boot-starter'
}
```

---

## 4. 도메인 간 통신 패턴

### 4.1 직접 호출 (Direct Invocation)

**장점**
- 구현이 간단함
- 동기식 처리로 즉각적인 결과 확인
- 트랜잭션 관리가 용이

**단점**
- 도메인 간 강한 결합
- 순환 의존성 발생 가능
- 확장성 제한

**사용 예시: 주문 생성 시 사용자/상점 검증**
```java
@Service
public class OrderService {
    private final UserService userService;   // 직접 의존
    private final ShopService shopService;   // 직접 의존

    @Transactional
    public Order createOrder(Long userId, Long shopId, ...) {
        // 도메인 간 직접 호출로 검증
        if (!userService.existsById(userId)) {
            throw new NotFoundException("주문자를 찾을 수 없습니다.");
        }
        if (!shopService.existsById(shopId)) {
            throw new NotFoundException("상점을 찾을 수 없습니다.");
        }
        // 주문 생성 로직
    }
}
```

### 4.2 이벤트 기반 통신 (Event-Driven)

**장점**
- 도메인 간 느슨한 결합
- 비동기 처리 가능
- 확장성이 뛰어남

**단점**
- 구현 복잡도 증가
- 이벤트 순서 보장 어려움
- 디버깅 어려움

**사용 예시: 도메인 이벤트 정의**
```java
// 이벤트 정의
public class UserCreatedEvent {
    private final Long userId;
    private final String email;
    private final LocalDateTime occurredAt;

    // 생성자, getter
}

// 이벤트 발행
@Service
public class UserService {
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public User createUser(...) {
        User savedUser = userRepository.save(user);

        // 이벤트 발행
        eventPublisher.publishEvent(
            new UserCreatedEvent(savedUser.getId(), savedUser.getEmail())
        );

        return savedUser;
    }
}

// 이벤트 구독
@Component
public class UserEventListener {

    @EventListener
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        log.info("사용자 생성 이벤트 수신: {}", event);
        // 비즈니스 로직 처리
    }
}
```

### 4.3 통신 패턴 선택 가이드

| 상황 | 권장 패턴 | 이유 |
|------|----------|------|
| 데이터 검증 필요 | 직접 호출 | 즉각적인 결과 필요 |
| 트랜잭션 일관성 필요 | 직접 호출 | 동일 트랜잭션 내 처리 |
| 알림/로깅 | 이벤트 기반 | 비동기 처리 적합 |
| 통계 업데이트 | 이벤트 기반 | 느슨한 결합 필요 |
| 외부 시스템 연동 | 이벤트 기반 | 장애 격리 필요 |

---

## 5. 도메인 이벤트 목록

### 5.1 User Domain
| 이벤트 | 발행 시점 | 구독자 |
|--------|----------|--------|
| UserCreatedEvent | 사용자 생성 후 | OrderService, ShopService |

### 5.2 Shop Domain
| 이벤트 | 발행 시점 | 구독자 |
|--------|----------|--------|
| ShopCreatedEvent | 상점 생성 후 | OrderService |

### 5.3 Order Domain
| 이벤트 | 발행 시점 | 구독자 |
|--------|----------|--------|
| OrderCreatedEvent | 주문 생성 후 | (알림, 통계 등) |
| OrderCancelledEvent | 주문 취소 후 | (재고 복원, 환불 등) |

---

## 6. 주의사항

### 6.1 순환 의존성 방지
```
❌ 잘못된 예시:
user-service → order-service
order-service → user-service  (순환!)

✅ 올바른 예시:
order-service → user-service (직접 의존)
user-service → order-service 대신 이벤트로 통신
```

### 6.2 의존성 방향 원칙
1. **핵심 도메인**은 다른 도메인에 의존하지 않음
2. **부가 도메인**은 핵심 도메인에 의존 가능
3. **이벤트**는 발행하는 도메인에 정의

### 6.3 트랜잭션 경계
- 각 도메인은 자체 트랜잭션 경계를 가짐
- 도메인 간 트랜잭션이 필요한 경우 Saga 패턴 고려

---

## 7. 확장 가이드

### 7.1 새 도메인 추가 시
1. `{domain-name}-domain` 디렉토리 생성
2. 하위 모듈 생성: `{domain-name}-api`, `{domain-name}-service`, `{domain-name}-infrastructure`
3. `settings.gradle`에 모듈 등록
4. 각 모듈의 `build.gradle` 작성
5. 필요한 도메인 의존성 설정

### 7.2 마이크로서비스 전환 시
현재 구조는 마이크로서비스로 쉽게 전환 가능:
1. 각 도메인을 독립 Spring Boot 애플리케이션으로 분리
2. 이벤트 기반 통신을 Kafka/RabbitMQ로 교체
3. 직접 호출을 REST API 또는 gRPC로 교체

---

## 8. 실습 체크리스트

- [x] 도메인별 모듈 구조 이해
- [x] user-domain 모듈 구현
- [x] shop-domain 모듈 구현
- [x] order-domain 모듈 구현
- [x] 도메인 간 직접 호출 구현
- [x] 도메인 이벤트 정의 및 발행
- [x] 이벤트 리스너 구현
- [ ] 빌드 및 테스트 실행

---

## 다음 단계

**Phase 4: 고급 멀티 모듈 패턴**
- 헥사고날 아키텍처 (Ports & Adapters)
- 클린 아키텍처 적용
- 모듈별 독립 빌드 및 배포
