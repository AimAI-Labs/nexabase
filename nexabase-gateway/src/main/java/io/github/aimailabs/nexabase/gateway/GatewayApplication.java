package io.github.aimailabs.nexabase.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Nexabase 网关服务启动类。
 * <p>注：Spring Cloud 2025+ 中 @EnableDiscoveryClient 为可选注解，
 * 当 classpath 存在 discovery 依赖时会自动激活。此处显式声明以表明意图。
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
