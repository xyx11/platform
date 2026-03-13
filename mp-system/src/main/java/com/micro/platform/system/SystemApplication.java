package com.micro.platform.system;

import com.alibaba.druid.spring.boot3.autoconfigure.DruidDataSourceAutoConfigure;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * 系统服务启动类
 */
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    DruidDataSourceAutoConfigure.class,
    SecurityAutoConfiguration.class
})
@EnableDiscoveryClient
@MapperScan("com.micro.platform.**.mapper")
@ComponentScan(basePackages = {"com.micro.platform.**"},
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = com.micro.platform.common.security.config.SaTokenConfig.class))
public class SystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class, args);
    }
}
