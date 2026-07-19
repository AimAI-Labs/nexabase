package io.github.aimailabs.nexabase.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Nexabase 认证授权服务启动类。
 * <p>
 * 承载 RBAC 权限管理、JWT 双 Token 认证、操作日志等核心能力。
 */
@SpringBootApplication
@MapperScan("io.github.aimailabs.nexabase.auth.mapper")
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
