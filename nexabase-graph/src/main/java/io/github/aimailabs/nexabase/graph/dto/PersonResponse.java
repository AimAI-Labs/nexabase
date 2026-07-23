package io.github.aimailabs.nexabase.graph.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 人物响应 DTO。
 * <p>
 * 不包含 knows 关系字段，避免序列化时的循环引用。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonResponse {

    /** 人物节点 ID */
    private Long id;

    /** 人物姓名 */
    private String name;

    /** 人物年龄 */
    private Integer age;
}
