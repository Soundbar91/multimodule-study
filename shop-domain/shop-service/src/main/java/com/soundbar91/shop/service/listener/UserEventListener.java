package com.soundbar91.shop.service.listener;

import com.soundbar91.user.domain.event.UserCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * User 도메인 이벤트 리스너
 * 이벤트 기반 통신을 통한 도메인 간 느슨한 결합 예시
 */
@Component
public class UserEventListener {

    private static final Logger log = LoggerFactory.getLogger(UserEventListener.class);

    /**
     * 사용자 생성 이벤트 처리
     * 예: 판매자 역할 사용자의 경우 상점 생성 안내 등
     */
    @EventListener
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        log.info("Shop 도메인에서 사용자 생성 이벤트 수신: {}", event);
        // 실제 구현에서는 여기에 비즈니스 로직 추가
        // 예: 판매자 역할 사용자에게 상점 등록 안내
        //     사용자-상점 매핑 캐시 갱신
    }
}
