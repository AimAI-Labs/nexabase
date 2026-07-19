package io.github.aimailabs.nexabase.foundation.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解。
 * <p>
 * 标注于 Controller 方法上，由 {@link OperLogAspect} 拦截采集操作信息，
 * 通过 {@link OperLogRecorder} 异步落库至 {@code sys_oper_log} 表。
 * <p>
 * 采集内容：操作人、模块、动作、HTTP 方法、URL、IP、请求参数（脱敏）、
 * 响应结果（可选）、耗时、状态（成功/失败）、错误信息。
 * <p>
 * 使用示例：
 * <pre>{@code
 * @Log(module = "用户管理", action = "新增用户")
 * @PostMapping
 * public Result<Void> create(@RequestBody UserCreateRequest request) { ... }
 *
 * @Log(module = "用户管理", action = "重置密码", saveResponse = true)
 * @PutMapping("/{id}/password/reset")
 * public Result<Void> resetPassword(@PathVariable Long id) { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {

    /**
     * 业务模块名称（如「用户管理」）。
     *
     * @return 模块名称
     */
    String module();

    /**
     * 操作动作描述（如「新增用户」、「重置密码」）。
     *
     * @return 动作描述
     */
    String action();

    /**
     * 是否记录请求参数（脱敏后）。
     *
     * @return 默认 {@code true}
     */
    boolean saveRequest() default true;

    /**
     * 是否记录响应结果（截断至 2000 字符）。
     * <p>
     * 默认关闭，避免大数据量响应占用日志存储。
     *
     * @return 默认 {@code false}
     */
    boolean saveResponse() default false;
}
