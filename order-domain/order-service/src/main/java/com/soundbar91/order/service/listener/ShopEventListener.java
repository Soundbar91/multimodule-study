package com.soundbar91.order.service.listener;

import com.soundbar91.shop.domain.event.ShopCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Shop 도메인 이벤트 리스너
 * 이벤트 기반 통신을 통한 도메인 간 느슨한 결합 예시
 */
@Component
public class ShopEventListener {

    private static final Logger log = LoggerFactory.getLogger(ShopEventListener.class);

    /**
     * 상점 생성 이벤트 처리
     * 예: 새 상점 알림, 통계 업데이트 등
     */
    @EventListener
    public void handleShopCreatedEvent(ShopCreatedEvent event) {
        log.info("상점 생성 이벤트 수신: {}", event);
        // 실제 구현에서는 여기에 비즈니스 로직 추가
        // 예: 새 상점 오픈 알림 발송
        //     상점 통계 업데이트
        //     추천 시스템 연동
    }
}
