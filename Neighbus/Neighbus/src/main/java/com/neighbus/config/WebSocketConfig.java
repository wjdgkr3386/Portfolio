package com.neighbus.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")
        .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // [1] 구독(수신) 접두사 (공개 토픽)
        registry.enableSimpleBroker("/sub"); 
        
        // [2] ✨ 개인 사용자별 큐 접두사 설정 (알림 핵심)
        registry.setUserDestinationPrefix("/user"); 

        // [3] 발행(송신) 접두사
        registry.setApplicationDestinationPrefixes("/pub");
    }
}