package io.github.aimailabs.nexabase.gateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 修复 Spring Boot 3.5 / Spring Cloud 2025 下，Knife4j 4.5.0 无法找到
 * DiscoveryClientRouteDefinitionLocator Bean 的问题。
 */
@Configuration
public class Knife4jFixConfig {

    @Bean
    @ConditionalOnMissingBean
    public DiscoveryClientRouteDefinitionLocator discoveryClientRouteDefinitionLocator(
            ReactiveDiscoveryClient discoveryClient, 
            DiscoveryLocatorProperties properties) {
        return new DiscoveryClientRouteDefinitionLocator(discoveryClient, properties);
    }
}
