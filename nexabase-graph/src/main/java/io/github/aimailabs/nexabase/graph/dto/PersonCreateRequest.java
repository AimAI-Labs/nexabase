package io.github.aimailabs.nexabase.graph.dto;

import lombok.Data;

/**
 * 创建人物请求。
 */
@Data
public class PersonCreateRequest {

    /** 人物姓名 */
    private String name;

    /** 人物年龄 */
    private Integer age;
}
