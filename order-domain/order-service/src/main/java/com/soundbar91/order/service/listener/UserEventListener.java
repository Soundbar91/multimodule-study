package com.soundbar91.order.service.listener;

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
     * 예: 신규 사용자에게 환영 쿠폰 발급, 알림 발송 등
     */
    @EventListener
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        log.info("사용자 생성 이벤트 수신: {}", event);
        // 실제 구현에서는 여기에 비즈니스 로직 추가
        // 예: 신규 사용자 환영 쿠폰 발급
        //     사용자 통계 업데이트
        //     마케팅 시스템 연동
    }
}
