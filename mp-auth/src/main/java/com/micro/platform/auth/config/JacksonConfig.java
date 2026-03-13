package com.micro.platform.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson 配置 - 禁用默认类型信息以避免反序列化问题
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "objectMapper")
    public ObjectMapper webObjectMapper(Jackson2ObjectMapperBuilder builder) {
        // 禁用默认类型信息，避免需要 @class 属性
        return builder.createXmlMapper(false).build();
    }
}