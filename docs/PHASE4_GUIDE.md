# Phase 4.3: 모듈별 독립 빌드 및 배포

## 학습 목표
- 모듈별 버전 관리 방법 이해
- bootJar vs jar의 차이점과 사용 시점 파악
- 통합 테스트 모듈 구성
- 전체/개별 모듈 빌드 실습

---

## 1. 모듈별 버전 관리

### 1.1 gradle.properties 활용
프로젝트 전체 및 모듈별 버전을 중앙에서 관리합니다.

```properties
# gradle.properties

# Project Version
projectVersion=0.0.1-SNAPSHOT
projectGroup=com.soundbar91

# Module Versions (독립적 버전 관리 가능)
commonVersion=0.0.1-SNAPSHOT
domainVersion=0.0.1-SNAPSHOT
apiVersion=0.0.1-SNAPSHOT

# Domain Versions
userDomainVersion=0.0.1-SNAPSHOT
shopDomainVersion=0.0.1-SNAPSHOT
orderDomainVersion=0.0.1-SNAPSHOT

# Build Settings
org.gradle.parallel=true
org.gradle.caching=true
```

### 1.2 모듈에서 버전 참조
```groovy
// 각 모듈의 build.gradle
version = rootProject.findProperty('commonVersion') ?: '0.0.1-SNAPSHOT'
```

### 1.3 독립 버전 관리의 장점
- **유연한 배포**: 변경된 모듈만 버전 업 가능
- **호환성 관리**: 모듈 간 버전 호환성 명시 가능
- **라이브러리 배포**: 특정 모듈을 외부 라이브러리로 배포 시 독립 버전 필요

---

## 2. bootJar vs jar

### 2.1 개념 비교

| 구분 | jar | bootJar |
|------|-----|---------|
| **용도** | 라이브러리 모듈 | 실행 가능한 애플리케이션 |
| **의존성** | 포함 안됨 | 모든 의존성 포함 (Fat JAR) |
| **실행** | `java -jar` 불가 | `java -jar` 실행 가능 |
| **크기** | 작음 (KB~MB) | 큼 (수십~수백 MB) |

### 2.2 모듈별 설정

#### 실행 가능한 모듈 (api 모듈)
```groovy
// api/build.gradle
plugins {
    id 'org.springframework.boot'
}

// Executable module: produces bootJar
bootJar {
    enabled = true
    archiveBaseName = 'api'
    archiveFileName = "api-${version}.jar"
    mainClass = 'com.soundbar91.api.ApiApplication'
}

jar {
    enabled = false  // 일반 jar 비활성화
}
```

#### 라이브러리 모듈 (common, domain, infrastructure 등)
```groovy
// common/build.gradle
plugins {
    id 'org.springframework.boot'
}

// Library module: produces jar, not bootJar
bootJar {
    enabled = false  // bootJar 비활성화
}

jar {
    enabled = true
    archiveBaseName = 'common'
}
```

### 2.3 빌드 결과 구조
```
build/libs/
├── api-0.0.1-SNAPSHOT.jar              # bootJar (54MB, 실행 가능)
├── common-0.0.1-SNAPSHOT-plain.jar     # jar (작음, 라이브러리)
├── domain-0.0.1-SNAPSHOT-plain.jar
├── infrastructure-0.0.1-SNAPSHOT-plain.jar
├── user-service-0.0.1-SNAPSHOT-plain.jar
└── ...
```

---

## 3. 통합 테스트 모듈

### 3.1 모듈 구조
```
integration-test/
├── build.gradle
└── src/test/
    ├── java/com/soundbar91/integration/
    │   ├── IntegrationTestBase.java    # 테스트 베이스 클래스
    │   └── ModuleDependencyTest.java   # 모듈 의존성 검증
    └── resources/
        └── application-test.yml        # 테스트 환경 설정
```

### 3.2 build.gradle 설정
```groovy
// integration-test/build.gradle
plugins {
    id 'org.springframework.boot'
}

dependencies {
    // 모든 모듈 의존
    testImplementation project(':common')
    testImplementation project(':domain')
    testImplementation project(':infrastructure')
    testImplementation project(':api')

    // Phase 3 도메인 모듈
    testImplementation project(':user-domain:user-api')
    testImplementation project(':user-domain:user-service')
    testImplementation project(':user-domain:user-infrastructure')
    // ... 기타 도메인 모듈

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

// 테스트 전용 모듈: jar 생성 안함
bootJar { enabled = false }
jar { enabled = false }
```

### 3.3 테스트 환경 설정
```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
```

---

## 4. 빌드 명령어

### 4.1 전체 빌드
```bash
# 전체 프로젝트 빌드 (테스트 제외)
./gradlew clean build -x test

# 전체 프로젝트 빌드 (테스트 포함)
./gradlew clean build
```

### 4.2 개별 모듈 빌드
```bash
# 특정 모듈만 빌드
./gradlew :api:build
./gradlew :user-domain:user-service:build

# 특정 모듈의 jar만 생성
./gradlew :common:jar
./gradlew :api:bootJar
```

### 4.3 의존성 확인
```bash
# 모듈의 의존성 트리 확인
./gradlew :api:dependencies --configuration runtimeClasspath

# 의존성 그래프 출력
./gradlew :api:dependencyInsight --dependency spring-boot
```

### 4.4 통합 테스트 실행
```bash
# 통합 테스트만 실행
./gradlew :integration-test:test

# 전체 테스트 실행
./gradlew test
```

---

## 5. 실행 및 배포

### 5.1 애플리케이션 실행
```bash
# bootJar 실행
java -jar api/build/libs/api-0.0.1-SNAPSHOT.jar

# 프로파일 지정 실행
java -jar api/build/libs/api-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### 5.2 라이브러리 배포 (Maven Local)
```bash
# 로컬 Maven 저장소에 배포
./gradlew :common:publishToMavenLocal

# 모든 라이브러리 모듈 배포
./gradlew publishToMavenLocal
```

---

## 6. Jar Manifest 설정

### 6.1 루트 build.gradle 설정
```groovy
subprojects {
    tasks.withType(Jar).configureEach {
        manifest {
            attributes(
                'Implementation-Title': project.name,
                'Implementation-Version': project.version,
                'Implementation-Vendor': 'soundbar91'
            )
        }
    }
}
```

### 6.2 Manifest 확인
```bash
# jar 파일의 MANIFEST.MF 확인
unzip -p common/build/libs/common-0.0.1-SNAPSHOT-plain.jar META-INF/MANIFEST.MF
```

---

## 7. 빌드 최적화

### 7.1 병렬 빌드
```properties
# gradle.properties
org.gradle.parallel=true
```

### 7.2 빌드 캐시
```properties
# gradle.properties
org.gradle.caching=true
```

### 7.3 JVM 메모리 설정
```properties
# gradle.properties
org.gradle.jvmargs=-Xmx2g -XX:+HeapDumpOnOutOfMemoryError
```

---

## 8. 체크리스트

### 8.1 버전 관리
- [ ] gradle.properties에 모듈별 버전 정의
- [ ] 각 모듈에서 버전 참조 설정
- [ ] 독립 버전 관리 필요 여부 검토

### 8.2 bootJar vs jar
- [ ] 실행 모듈에 bootJar 활성화
- [ ] 라이브러리 모듈에 jar 활성화
- [ ] archiveBaseName 설정

### 8.3 통합 테스트
- [ ] integration-test 모듈 생성
- [ ] 모든 도메인 모듈 의존성 추가
- [ ] 테스트 환경 설정 (application-test.yml)

### 8.4 빌드 검증
- [ ] 전체 빌드 성공 확인
- [ ] 개별 모듈 빌드 성공 확인
- [ ] bootJar 실행 가능 확인
- [ ] 의존성 그래프 검토

---

## 9. 주의사항

### 9.1 Spring Boot 4.0 변경사항
- `@EntityScan` 패키지 위치 변경
  - 이전: `org.springframework.boot.autoconfigure.domain.EntityScan`
  - 현재: `org.springframework.boot.persistence.autoconfigure.EntityScan`

### 9.2 순환 의존성 방지
```bash
# 순환 의존성 검사
./gradlew buildEnvironment
```

### 9.3 테스트 격리
- 통합 테스트는 별도 모듈에서 관리
- 단위 테스트는 각 모듈 내부에서 관리
- 테스트 간 데이터 격리 보장 (`@Transactional` 활용)

---

## 10. 다음 단계

Phase 4.3 완료 후 다음 학습 방향:
1. **Phase 4.1**: 헥사고날 아키텍처 적용
2. **Phase 4.2**: 클린 아키텍처 적용
3. **Phase 5**: 프로덕션 수준 설정 (보안, 모니터링)
