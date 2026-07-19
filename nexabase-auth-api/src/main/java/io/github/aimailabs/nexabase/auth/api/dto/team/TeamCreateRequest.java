package io.github.aimailabs.nexabase.auth.api.dto.team;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增/修改团队请求。
 */
@Data
public class TeamCreateRequest {

    /** 父团队 ID（0 为根节点） */
    @NotNull(message = "父团队 ID 不能为空")
    private Long parentId;

    @NotBlank(message = "团队名称不能为空")
    @Size(max = 64, message = "团队名称长度不能超过 64")
    private String name;

    /** 类型：1-部门, 2-项目组 */
    @NotNull(message = "团队类型不能为空")
    private Integer type;

    /** 排序号 */
    private Integer sortOrder = 0;
}
