package io.github.aimailabs.nexabase.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 网关 / Nacos 连通性测试接口
 * <p>
 * 用于校验网关与 Nacos 注册中心的连接状态（内部运维用途）。
 *
 * @module nexabase-gateway
 */
@RestController
@RequestMapping("/test/nacos")
public class NacosTestController {

    @Autowired(required = false)
    private ReactiveDiscoveryClient reactiveDiscoveryClient;

    @Value("${spring.application.name:}")
    private String applicationName;

    /**
     * 检查网关与 Nacos 注册中心的连接状态。
     *
     * @return 包含应用名与已注册服务列表的连通性结果
     */
    @GetMapping("/status")
    public Mono<Map<String, Object>> getNacosStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("applicationName", applicationName);
        
        if (reactiveDiscoveryClient == null) {
            result.put("status", "warning");
            result.put("message", "ReactiveDiscoveryClient is not available. Nacos discovery might not be configured as reactive.");
            return Mono.just(result);
        }

        return reactiveDiscoveryClient.getServices().collectList()
                .map(services -> {
                    result.put("status", "success");
                    result.put("message", "Nacos connection is successful!");
                    result.put("services", services);
                    return result;
                })
                .onErrorResume(e -> {
                    result.put("status", "error");
                    result.put("message", "Failed to connect to Nacos: " + e.getMessage());
                    return Mono.just(result);
                });
    }
}
