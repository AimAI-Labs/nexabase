package io.github.aimailabs.nexabase.auth.init;

import io.github.aimailabs.nexabase.auth.entity.SysUser;
import io.github.aimailabs.nexabase.auth.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码初始化器。
 * <p>
 * Flyway 种子数据中 admin 用户的 {@code password_hash} 为占位符 {@code INIT_PASSWORD_HASH}，
 * 启动时检测到占位符则用 BCrypt 编码 {@code 123456} 替换，确保密码以正确哈希存储。
 * <p>
 * 仅首次启动（占位符存在时）执行，后续启动跳过。
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class PasswordDataInitializer implements CommandLineRunner {

    private static final String INIT_PLACEHOLDER = "INIT_PASSWORD_HASH";

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        SysUser admin = userMapper.selectById(1L);
        if (admin != null && INIT_PLACEHOLDER.equals(admin.getPasswordHash())) {
            admin.setPasswordHash(passwordEncoder.encode("123456"));
            userMapper.updateById(admin);
            log.info("admin 默认密码已初始化为 BCrypt 哈希");
        }
    }
}
