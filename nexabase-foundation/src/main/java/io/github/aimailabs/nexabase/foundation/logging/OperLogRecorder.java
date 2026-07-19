package io.github.aimailabs.nexabase.foundation.logging;

/**
 * 操作日志落库 SPI 接口。
 * <p>
 * 由 {@link OperLogAspect} 调用，将采集的 {@link OperLogContext} 持久化。
 * 默认实现 {@code SysOperLogRecorder} 位于 {@code nexabase-auth} 模块，
 * 通过独立线程池异步写入 {@code sys_oper_log} 表，并预留 MQ 投递点。
 * <p>
 * 业务服务若需记录操作日志，实现此接口并注册为 Bean 即可被 {@link OperLogAspect} 自动使用；
 * 未提供实现时，切面仅输出 WARN 日志，不阻塞业务。
 */
public interface OperLogRecorder {

    /**
     * 异步记录操作日志。
     * <p>
     * 实现应保证非阻塞，不影响业务请求响应。
     *
     * @param context 操作日志上下文
     */
    void record(OperLogContext context);
}
