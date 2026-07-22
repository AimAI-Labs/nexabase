package io.github.aimailabs.nexabase.file.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import io.github.aimailabs.nexabase.foundation.security.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充配置。
 * <p>
 * 自动填充审计字段：创建时间、更新时间、创建人、更新人。
 * <ul>
 *   <li>INSERT 时自动填充 {@code createdAt}、{@code createdBy}</li>
 *   <li>INSERT/UPDATE 时自动填充 {@code updatedAt}、{@code updatedBy}</li>
 * </ul>
 * 要求实体字段标注 {@code @TableField(fill = FieldFill.INSERT)} 等注解。
 */
@Slf4j
@Configuration
public class MybatisPlusConfig implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        Long userId = UserContext.getCurrentUserId();

        // 自动填充创建时间和更新时间
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);

        // 自动填充创建人和更新人（若当前用户存在）
        if (userId != null) {
            this.strictInsertFill(metaObject, "createdBy", Long.class, userId);
            this.strictInsertFill(metaObject, "updatedBy", Long.class, userId);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        Long userId = UserContext.getCurrentUserId();

        // 自动填充更新时间
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, now);

        // 自动填充更新人（若当前用户存在）
        if (userId != null) {
            this.strictUpdateFill(metaObject, "updatedBy", Long.class, userId);
        }
    }
}
