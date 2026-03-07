package com.micro.platform.common.swagger.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Knife4j/Swagger 配置
 */
@Configuration
public class SwaggerConfig {

    @Value("${spring.application.name:Micro Platform}")
    private String applicationName;

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @Bean
    public OpenApiCustomizer customOpenAPI() {
        return openAPI -> {
            // 服务信息
            openAPI.info(new Info()
                    .title(applicationName + " API")
                    .version("1.0.0")
                    .description("Java EE 分布式微服务架构平台 API 文档")
                    .contact(new Contact()
                            .name("Micro Platform")
                            .email("admin@micro-platform.com"))
                    .license(new License()
                            .name("Apache 2.0")
                            .url("https://www.apache.org/licenses/LICENSE-2.0")));

            // 添加认证方案
            openAPI.schemaRequirement("Authorization", new SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .name("Authorization")
                    .in(SecurityScheme.In.HEADER)
                    .description("Bearer Token 认证"));

            // 全局安全要求
            List<SecurityRequirement> securityRequirements = new ArrayList<>();
            securityRequirements.add(new SecurityRequirement().addList("Authorization"));
            openAPI.security(securityRequirements);

            // 服务器
            List<Server> servers = new ArrayList<>();
            Server server = new Server();
            server.setUrl(contextPath);
            server.setDescription("本地服务");
            servers.add(server);
            openAPI.servers(servers);
        };
    }
}
