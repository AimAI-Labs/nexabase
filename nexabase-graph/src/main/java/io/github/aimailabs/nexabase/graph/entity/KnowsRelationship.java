package io.github.aimailabs.nexabase.graph.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

/**
 * KNOWS 关系实体
 * <p>
 * 演示 SDN 的 @RelationshipProperties 模式：
 * - @TargetNode 指向关系的目标节点
 * - 普通字段映射为关系属性（since = 认识于哪一年）
 * <p>
 * 父节点 PersonNode 通过 @Relationship(type="KNOWS", direction=OUTGOING)
 * 引用 List&lt;KnowsRelationship&gt;，形成聚合根。
 * <p>
 * 注意：此处不使用 @Data（含 @ToString/@EqualsAndHashCode），
 * 因关系实体与 PersonNode 存在循环引用，避免 StackOverflowError。
 */
@RelationshipProperties
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowsRelationship {

    /**
     * SDN 7.x 要求：@RelationshipProperties 类必须包含 @Id @GeneratedValue 字段，
     * 用于安全地更新关系属性（如 since）。
     */
    @Id
    @GeneratedValue
    private Long id;

    private Integer since;

    @TargetNode
    private PersonNode target;

    /**
     * 业务构造器：创建新关系时使用，id 由 SDN @GeneratedValue 自动生成。
     */
    public KnowsRelationship(Integer since, PersonNode target) {
        this.since = since;
        this.target = target;
    }
}
