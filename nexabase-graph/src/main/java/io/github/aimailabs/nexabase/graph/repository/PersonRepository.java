package io.github.aimailabs.nexabase.graph.repository;

import io.github.aimailabs.nexabase.graph.entity.PersonNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Person 节点 Repository
 * <p>
 * 继承 Neo4jRepository 自动获得 CRUD 方法（save, findById, findAll, delete 等）。
 * 自定义方法演示两种查询方式：
 * - 派生查询（findByName）：SDN 根据方法名自动生成 Cypher
 * - @Query 自定义 Cypher（findFriendsOfFriends）：手写 Cypher 处理多跳遍历
 */
public interface PersonRepository extends Neo4jRepository<PersonNode, Long> {

    /**
     * 按姓名查找人物（派生查询，SDN 自动生成 Cypher）
     * 默认加载深度 1，会带出 KNOWS 关系
     */
    Optional<PersonNode> findByName(String name);

    /**
     * 查询朋友的朋友（2跳遍历）
     * <p>
     * 这是图数据库的经典场景：用一条 Cypher 完成多跳关系遍历，
     * 关系型数据库需要多表 JOIN 才能实现。
     * <p>
     * Cypher 解释：
     * MATCH (p {name})-[:KNOWS]->(:Person)-[:KNOWS]->(fof)
     *   → 从 p 出发，经过2跳 KNOWS 关系到达 fof
     * WHERE fof <> p
     *   → 排除自己
     * RETURN DISTINCT fof
     *   → 去重后返回朋友的朋友
     */
    @Query("MATCH (p:Person {name: $name})-[:KNOWS]->(:Person)-[:KNOWS]->(fof:Person) " +
           "WHERE fof <> p " +
           "RETURN DISTINCT fof")
    List<PersonNode> findFriendsOfFriends(@Param("name") String name);
}
