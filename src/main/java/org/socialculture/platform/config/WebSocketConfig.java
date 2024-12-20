package org.socialculture.platform.config;

import lombok.RequiredArgsConstructor;
import org.socialculture.platform.chat.websocket.ChatWebSocketHandler;
import org.socialculture.platform.member.auth.JwtTokenProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

/**
 * 채팅용 웹 소켓 설정 파일
 * 
 * @author ycjung
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final ChatWebSocketHandler chatWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat", "/ws/chat/chat-list")
                .setAllowedOrigins("*")  // CORS 설정, 모든 도메인 허용
                .addInterceptors(new HttpSessionHandshakeInterceptor());  // WebSocket 핸드셰이크 시 세션 정보 유지
    }
}
