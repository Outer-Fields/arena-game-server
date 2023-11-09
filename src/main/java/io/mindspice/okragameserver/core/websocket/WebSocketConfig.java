package io.mindspice.okragameserver.core.websocket;

import io.mindspice.okragameserver.util.Log;
import jakarta.annotation.PostConstruct;
import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;


@Configuration
@EnableWebSocket
//@EnableWebSocketMessageBroker
public class WebSocketConfig  implements WebSocketConfigurer {

    private final GameServerSocketHandler gameServerSocketHandler;
    private final TokenHandshakeInterceptor tokenHandshakeInterceptor;

    public WebSocketConfig(GameServerSocketHandler gameServerSocketHandler, TokenHandshakeInterceptor tokenHandshakeInterceptor) {
        this.gameServerSocketHandler = gameServerSocketHandler;
        this.tokenHandshakeInterceptor = tokenHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gameServerSocketHandler, "/ws")
                .setAllowedOrigins("*")
                .addInterceptors(tokenHandshakeInterceptor);
    }

}