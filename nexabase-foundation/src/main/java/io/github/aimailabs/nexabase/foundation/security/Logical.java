package io.github.aimailabs.nexabase.foundation.security;

/**
 * 权限/角色校验的逻辑组合方式。
 * <ul>
 *   <li>{@link #AND} — 必须同时满足所有条件</li>
 *   <li>{@link #OR} — 满足任意一个条件即可</li>
 * </ul>
 */
public enum Logical {

    /** 全部满足 */
    AND,

    /** 任一满足 */
    OR
}
