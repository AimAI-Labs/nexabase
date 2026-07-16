package io.github.aimailabs.nexabase.graph.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

/**
 * Person 节点实体
 * <p>
 * 演示 SDN 的 @Node 注解，映射 Neo4j 中的 Person 标签节点。
 * 通过 @Relationship 定义 KNOWS 关系（有向、OUTGOING），
 * 使用 @RelationshipProperties 模式以支持关系属性（since）。
 * <p>
 * 注意：此处不使用 @Data（含 @ToString/@EqualsAndHashCode），
 * 因 knows 列表与 KnowsRelationship 存在循环引用，避免 StackOverflowError。
 */
@Node("Person")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonNode {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private Integer age;

    @Relationship(type = "KNOWS", direction = Relationship.Direction.OUTGOING)
    private List<KnowsRelationship> knows = new ArrayList<>();

    public PersonNode(String name, Integer age) {
        this.name = name;
        this.age = age;
    }
}
