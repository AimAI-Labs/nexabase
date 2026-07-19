package io.github.aimailabs.nexabase.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.github.aimailabs.nexabase.auth.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 团队/组织实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_team")
public class SysTeam extends BaseEntity {

    /** 父团队 ID(0 为根节点) */
    private Long parentId;

    /** 团队名称 */
    private String name;

    /** 类型: 1-部门, 2-项目组 */
    private Integer type;

    /** 排序号 */
    private Integer sortOrder;
}
