package io.github.aimailabs.nexabase.foundation.security;

/**
 * 当前请求用户上下文。
 * <p>
 * 基于 {@link ThreadLocal} 在当前线程内传递登录用户信息，由
 * {@link ServletUserContextFilter} 在请求入口从 HTTP Header（{@code X-User-Id} / {@code X-User-Name}）
 * 解析并写入。请求结束时由 Filter 清除，防止线程池复用导致串扰。
 * <p>
 * 同时将 {@code userId} 写入 MDC（复用 {@link io.github.aimailabs.nexabase.foundation.trace.TraceContext}），
 * 使日志可通过 {@code %X{userId}} 按用户过滤。
 * <p>
 * 业务代码通过此类获取当前操作人，用于：
 * <ul>
 *   <li>审计字段自动填充（{@code createdBy} / {@code updatedBy}）</li>
 *   <li>操作日志记录操作人</li>
 *   <li>业务逻辑中获取当前用户标识</li>
 * </ul>
 */
public final class UserContext {

    /** 网关透传用户 ID 的 HTTP 头名称 */
    public static final String USER_ID_HEADER = "X-User-Id";

    /** 网关透传用户名的 HTTP 头名称 */
    public static final String USER_NAME_HEADER = "X-User-Name";

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_NAME = new ThreadLocal<>();

    private UserContext() {
    }

    /**
     * 获取当前登录用户 ID。
     *
     * @return 用户 ID，未登录或非 HTTP 请求上下文返回 {@code null}
     */
    public static Long getCurrentUserId() {
        return USER_ID.get();
    }

    /**
     * 获取当前登录用户名。
     *
     * @return 用户名，未登录或非 HTTP 请求上下文返回 {@code null}
     */
    public static String getCurrentUserName() {
        return USER_NAME.get();
    }

    /**
     * 写入当前用户信息（供 Filter 调用）。
     *
     * @param userId   用户 ID
     * @param userName 用户名
     */
    static void set(Long userId, String userName) {
        if (userId != null) {
            USER_ID.set(userId);
        }
        if (userName != null) {
            USER_NAME.set(userName);
        }
    }

    /**
     * 清除当前线程的用户上下文（防止线程池复用串扰）。
     */
    static void clear() {
        USER_ID.remove();
        USER_NAME.remove();
    }
}
