package io.github.aimailabs.nexabase.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.github.aimailabs.nexabase.auth.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    /** 登录账号 */
    private String username;

    /** BCrypt 哈希密码 */
    private String passwordHash;

    /** 用户昵称 */
    private String nickname;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 状态: 1-正常, 0-禁用 */
    private Integer status;

    /** JWT 版本号(用于批量让 Token 失效) */
    private Long jwtVersion;
}
