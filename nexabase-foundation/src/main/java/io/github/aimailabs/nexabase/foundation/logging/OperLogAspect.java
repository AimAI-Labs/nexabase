package io.github.aimailabs.nexabase.foundation.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.aimailabs.nexabase.foundation.security.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 操作日志切面。
 * <p>
 * 拦截 {@link Log} 注解标注的 Controller 方法，采集操作信息构建 {@link OperLogContext}，
 * 通过 {@link OperLogRecorder}（可选注入）异步落库。
 * <p>
 * 采集内容：操作人（{@link UserContext}）、模块/动作（注解）、HTTP 方法/URL/IP（请求）、
 * 请求参数（脱敏）、响应结果（可选截断）、耗时、状态、错误信息。
 * <p>
 * 未注入 {@link OperLogRecorder} 时仅输出 WARN 日志，不阻塞业务。
 */
@Slf4j
@Aspect
public class OperLogAspect {

    /** 敏感字段名匹配正则（password/secret/token 等） */
    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
            "(\"(?:password|oldPassword|newPassword|token|secret|credential|apiKey)\"\\s*:\\s*)\"[^\"]*\"",
            Pattern.CASE_INSENSITIVE);

    /** 响应结果最大保存长度 */
    private static final int MAX_RESPONSE_LENGTH = 2000;

    /** 不可序列化的参数类型（跳过记录） */
    private static final Set<Class<?>> EXCLUDED_PARAM_TYPES = Set.of(
            HttpServletRequest.class,
            jakarta.servlet.http.HttpServletResponse.class
    );

    private final ObjectProvider<OperLogRecorder> recorderProvider;
    private final ObjectMapper objectMapper;

    public OperLogAspect(ObjectProvider<OperLogRecorder> recorderProvider, ObjectMapper objectMapper) {
        this.recorderProvider = recorderProvider;
        this.objectMapper = objectMapper.copy()
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    @Around("@annotation(io.github.aimailabs.nexabase.foundation.logging.Log)")
    public Object aroundLog(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Log logAnnotation = method.getAnnotation(Log.class);
        long startTime = System.currentTimeMillis();

        Throwable error = null;
        Object result = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            error = e;
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            try {
                record(joinPoint, logAnnotation, result, error, costTime);
            } catch (Exception e) {
                log.warn("操作日志记录失败，不影响业务: {}", e.getMessage());
            }
        }
    }

    private void record(ProceedingJoinPoint joinPoint, Log logAnnotation,
                        Object result, Throwable error, long costTime) {
        OperLogRecorder recorder = recorderProvider.getIfAvailable();
        if (recorder == null) {
            log.warn("未注入 OperLogRecorder，操作日志未落库。模块={}, 动作={}",
                    logAnnotation.module(), logAnnotation.action());
            return;
        }

        HttpServletRequest request = getCurrentRequest();
        OperLogContext.OperLogContextBuilder builder = OperLogContext.builder()
                .userId(UserContext.getCurrentUserId())
                .username(UserContext.getCurrentUserName())
                .module(logAnnotation.module())
                .action(logAnnotation.action())
                .costTime(costTime)
                .status(error == null ? 1 : 0)
                .errorMsg(error != null ? truncate(error.getMessage(), MAX_RESPONSE_LENGTH) : null);

        if (request != null) {
            builder.method(request.getMethod())
                    .url(request.getRequestURI())
                    .ipAddress(getClientIp(request));
        }

        if (logAnnotation.saveRequest()) {
            builder.requestParams(serializeAndMask(joinPoint.getArgs()));
        }

        if (logAnnotation.saveResponse() && result != null) {
            builder.responseResult(truncate(serialize(result), MAX_RESPONSE_LENGTH));
        }

        recorder.record(builder.build());
    }

    private HttpServletRequest getCurrentRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            return servletAttrs.getRequest();
        }
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String serializeAndMask(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        Object[] filtered = Arrays.stream(args)
                .filter(arg -> arg == null || !EXCLUDED_PARAM_TYPES.contains(arg.getClass()))
                .toArray();
        try {
            String json = objectMapper.writeValueAsString(filtered);
            return SENSITIVE_PATTERN.matcher(json).replaceAll("$1\"****\"");
        } catch (Exception e) {
            return "[序列化失败: " + e.getMessage() + "]";
        }
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "[序列化失败: " + e.getMessage() + "]";
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() > maxLength ? value.substring(0, maxLength) + "..." : value;
    }
}
