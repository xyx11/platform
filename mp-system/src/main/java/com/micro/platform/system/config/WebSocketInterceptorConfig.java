package com.micro.platform.system.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 拦截器配置
 */
@Configuration
public class WebSocketInterceptorConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketInterceptorConfig.class);

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.taskExecutor();
    }
}