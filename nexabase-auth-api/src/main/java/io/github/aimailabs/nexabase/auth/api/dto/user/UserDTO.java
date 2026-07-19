package io.github.aimailabs.nexabase.auth.api.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户详情视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private Integer status;
    private LocalDateTime createdAt;

    /** 直接分配的角色 ID 列表 */
    private List<Long> roleIds;

    /** 所属团队 ID 列表 */
    private List<Long> teamIds;
}
