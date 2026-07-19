package io.github.aimailabs.nexabase.auth.api.dto.assign;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 分配角色请求。
 * <p>
 * 可用于：用户分配直接角色（targetId=userId）、团队继承角色（targetId=teamId）。
 */
@Data
public class AssignRolesRequest {

    /** 目标 ID（用户 ID 或团队 ID） */
    @NotNull(message = "目标 ID 不能为空")
    private Long targetId;

    /** 角色 ID 列表（全量覆盖） */
    @NotEmpty(message = "角色 ID 列表不能为空")
    private List<Long> roleIds;
}
