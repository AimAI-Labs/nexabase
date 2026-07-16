package io.github.aimailabs.nexabase.graph.dto;

import lombok.Data;

/**
 * 建立 KNOWS 关系请求
 */
@Data
public class CreateRelationshipRequest {

    private String fromName;
    private String toName;
    private Integer since;
}
