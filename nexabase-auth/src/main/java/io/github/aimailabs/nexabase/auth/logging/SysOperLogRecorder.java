package io.github.aimailabs.nexabase.auth.logging;

import io.github.aimailabs.nexabase.auth.service.SysOperLogService;
import io.github.aimailabs.nexabase.foundation.logging.OperLogContext;
import io.github.aimailabs.nexabase.foundation.logging.OperLogRecorder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * auth 模块操作日志落库实现。
 * <p>
 * 实现 {@link OperLogRecorder} SPI，将 {@link OperLogContext} 交由
 * {@link SysOperLogService} 异步写入 {@code sys_oper_log} 表。
 * <p>
 * 标注 {@link Primary} 确保在 auth 模块内优先于其他实现被注入到 {@code OperLogAspect}。
 */
@Component
@Primary
@RequiredArgsConstructor
public class SysOperLogRecorder implements OperLogRecorder {

    private final SysOperLogService operLogService;

    @Override
    public void record(OperLogContext context) {
        operLogService.recordAsync(context);
    }
}
