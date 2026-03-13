package com.micro.platform.system.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson 配置 - 禁用默认类型信息以避免前端解析问题
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "objectMapper")
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        // 禁用默认类型信息，避免返回 ["java.lang.Long", 2] 这种格式
        return builder.createXmlMapper(false).build();
    }
}