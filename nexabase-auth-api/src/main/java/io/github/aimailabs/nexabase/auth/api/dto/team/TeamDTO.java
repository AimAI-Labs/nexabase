package io.github.aimailabs.nexabase.auth.api.dto.team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 团队视图（树形结构）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamDTO {

    private Long id;
    private Long parentId;
    private String name;

    /** 类型：1-部门, 2-项目组 */
    private Integer type;

    /** 排序号 */
    private Integer sortOrder;

    /** 继承的角色 ID 列表 */
    private List<Long> roleIds;

    /** 子团队列表 */
    @Builder.Default
    private List<TeamDTO> children = new ArrayList<>();
}
