package com.soundbar91.config;

import com.soundbar91.config.properties.AppProperties;
import com.soundbar91.config.properties.DatabaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 공통 설정 Configuration 클래스
 * 이 클래스를 통해 config 모듈의 설정 속성들이 활성화됩니다.
 *
 * 사용법:
 * 1. 다른 모듈에서 config 모듈을 의존성으로 추가
 * 2. @Import(CommonConfig.class) 또는 ComponentScan으로 이 설정을 로드
 * 3. AppProperties, DatabaseProperties를 주입받아 사용
 */
@Configuration
@EnableConfigurationProperties({
        AppProperties.class,
        DatabaseProperties.class
})
public class CommonConfig {

}
