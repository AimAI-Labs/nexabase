package io.github.aimailabs.nexabase.auth.config;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import io.github.aimailabs.nexabase.foundation.security.UserContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 配置。
 * <p>
 * 注册分页插件与审计字段自动填充处理器。
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 分页插件（MySQL 方言）。
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(
                com.baomidou.mybatisplus.annotation.DbType.MYSQL));
        return interceptor;
    }

    /**
     * 审计字段自动填充处理器。
     * <p>
     * 在 INSERT/UPDATE 时自动填充 {@code createdAt}/{@code updatedAt}/{@code createdBy}/{@code updatedBy}，
     * 操作人从 {@link UserContext} 获取（由网关透传的 {@code X-User-Id} 解析）。
     */
    @Component
    public static class AuditMetaObjectHandler implements MetaObjectHandler {

        @Override
        public void insertFill(MetaObject metaObject) {
            LocalDateTime now = LocalDateTime.now();
            Long userId = UserContext.getCurrentUserId();
            this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
            this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
            this.strictInsertFill(metaObject, "createdBy", Long.class, userId);
            this.strictInsertFill(metaObject, "updatedBy", Long.class, userId);
        }

        @Override
        public void updateFill(MetaObject metaObject) {
            this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            this.strictUpdateFill(metaObject, "updatedBy", Long.class, UserContext.getCurrentUserId());
        }
    }
}
