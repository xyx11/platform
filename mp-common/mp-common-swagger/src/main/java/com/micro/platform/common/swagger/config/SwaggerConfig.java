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
 * 增强配置包括：全局认证、服务器列表、分组管理等
 */
@Configuration
public class SwaggerConfig {

    @Value("${spring.application.name:Micro Platform}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    /**
     * 自定义 OpenAPI 配置
     */
    @Bean
    public OpenApiCustomizer customOpenAPI() {
        return openAPI -> {
            // 服务信息
            openAPI.info(new Info()
                    .title(applicationName + " API")
                    .version("1.0.0")
                    .description("""
                        ## Java EE 分布式微服务架构平台 API 文档

                        ### 技术栈
                        - Spring Boot 3.x
                        - Spring Cloud 2023.x
                        - Spring Cloud Alibaba
                        - MyBatis Plus
                        - Sa-Token

                        ### 认证说明
                        使用 Bearer Token 进行认证，在请求头中添加:
                        ```
                        Authorization: Bearer <your-token>
                        ```
                        """)
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
                    .bearerFormat("JWT")
                    .description("""
                        **Bearer Token 认证**

                        登录接口获取 token 后，在请求头中添加:
                        ```
                        Authorization: Bearer <token>
                        ```

                        未登录或 token 过期会返回 401 错误。
                        """));

            // 全局安全要求（所有接口默认需要认证）
            List<SecurityRequirement> securityRequirements = new ArrayList<>();
            securityRequirements.add(new SecurityRequirement().addList("Authorization"));
            openAPI.security(securityRequirements);

            // 服务器列表
            List<Server> servers = new ArrayList<>();

            // 本地开发环境
            Server localServer = new Server();
            localServer.setUrl("http://localhost:" + serverPort + contextPath);
            localServer.setDescription("本地开发环境");
            servers.add(localServer);

            // 测试环境
            Server testServer = new Server();
            testServer.setUrl(contextPath);
            testServer.setDescription("测试环境");
            servers.add(testServer);

            openAPI.servers(servers);
        };
    }
}