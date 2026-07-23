package io.github.aimailabs.nexabase.graph.dto;

import lombok.Data;

/**
 * 建立 KNOWS 关系请求。
 */
@Data
public class CreateRelationshipRequest {

    /** 关系源人物姓名 */
    private String fromName;

    /** 关系目标人物姓名 */
    private String toName;

    /** 关系建立年份（关系属性 since） */
    private Integer since;
}
