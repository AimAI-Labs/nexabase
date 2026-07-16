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

@RestController
@RequestMapping("/test/nacos")
public class NacosTestController {

    @Autowired(required = false)
    private ReactiveDiscoveryClient reactiveDiscoveryClient;

    @Value("${spring.application.name:}")
    private String applicationName;

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
