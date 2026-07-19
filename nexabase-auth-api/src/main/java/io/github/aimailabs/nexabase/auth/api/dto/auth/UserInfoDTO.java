package io.github.aimailabs.nexabase.auth.api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 当前登录用户完整信息。
 * <p>
 * 供 {@code /userinfo} 接口返回，前端据此渲染菜单与控制按钮显隐。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {

    /** 用户 ID */
    private Long id;

    /** 登录账号 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 状态：1-正常, 0-禁用 */
    private Integer status;

    /** 拥有的角色 code 列表 */
    private List<String> roles;

    /** 拥有的权限标识列表（超管返回 {@code ["*"]}） */
    private List<String> permissions;

    /** 所属团队 ID 列表 */
    private List<Long> teamIds;
}
