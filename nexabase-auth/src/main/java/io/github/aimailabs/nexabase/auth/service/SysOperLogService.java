package io.github.aimailabs.nexabase.auth.service;

import io.github.aimailabs.nexabase.auth.entity.SysOperLog;
import io.github.aimailabs.nexabase.auth.mapper.SysOperLogMapper;
import io.github.aimailabs.nexabase.foundation.logging.OperLogContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务。
 * <p>
 * 通过独立线程池异步落库，不阻塞业务请求。
 * 预留 MQ 投递点：后续切换 statistics 消费时，在此方法内增加 {@code rabbitTemplate.convertAndSend} 即可。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysOperLogService {

    private final SysOperLogMapper operLogMapper;

    /**
     * 异步记录操作日志。
     * <p>
     * 使用 {@code operLogExecutor} 线程池，队列满时由调用线程执行（CallerRunsPolicy）。
     */
    @Async("operLogExecutor")
    public void recordAsync(OperLogContext context) {
        try {
            SysOperLog entity = new SysOperLog();
            entity.setUserId(context.getUserId());
            entity.setUsername(context.getUsername());
            entity.setModule(context.getModule());
            entity.setAction(context.getAction());
            entity.setMethod(context.getMethod());
            entity.setUrl(context.getUrl());
            entity.setIpAddress(context.getIpAddress());
            entity.setRequestParams(context.getRequestParams());
            entity.setResponseResult(context.getResponseResult());
            entity.setStatus(context.getStatus());
            entity.setErrorMsg(context.getErrorMsg());
            entity.setCostTime(context.getCostTime());
            operLogMapper.insert(entity);
        } catch (Exception e) {
            log.warn("操作日志落库失败，不影响业务: {}", e.getMessage());
        }
    }
}
