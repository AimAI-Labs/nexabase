package io.github.aimailabs.nexabase.graph.dto;

import lombok.Data;

/**
 * 创建人物请求
 */
@Data
public class PersonCreateRequest {

    private String name;
    private Integer age;
}
