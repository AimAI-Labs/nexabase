package io.github.aimailabs.nexabase.graph.service;

import io.github.aimailabs.nexabase.graph.dto.CreateRelationshipRequest;
import io.github.aimailabs.nexabase.graph.dto.PersonCreateRequest;
import io.github.aimailabs.nexabase.graph.entity.KnowsRelationship;
import io.github.aimailabs.nexabase.graph.entity.PersonNode;
import io.github.aimailabs.nexabase.graph.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Person 业务逻辑
 * <p>
 * 演示 SDN 聚合根模式：
 * - 节点 CRUD 通过 Repository 标准方法
 * - 关系创建通过加载聚合根 → 修改 knows 列表 → 保存聚合根
 * - 1跳遍历通过 Java 导航（person.getKnows()）
 * - 2跳遍历通过自定义 Cypher（repository.findFriendsOfFriends）
 */
@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository repository;

    /**
     * 创建人物节点
     */
    public PersonNode createPerson(PersonCreateRequest request) {
        PersonNode person = new PersonNode(request.getName(), request.getAge());
        return repository.save(person);
    }

    /**
     * 查询所有人物
     */
    public List<PersonNode> findAll() {
        return repository.findAll();
    }

    /**
     * 按姓名查询人物（含 KNOWS 关系）
     */
    public PersonNode findByName(String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("人物不存在: " + name));
    }

    /**
     * 建立 KNOWS 关系
     * <p>
     * SDN 聚合根模式：
     * 1. 加载源人物（含已有的 KNOWS 关系）
     * 2. 加载目标人物
     * 3. 将目标添加到源的 knows 列表
     * 4. 保存源人物 → SDN 自动同步关系到 Neo4j
     */
    public void createRelationship(CreateRelationshipRequest request) {
        PersonNode from = repository.findByName(request.getFromName())
                .orElseThrow(() -> new IllegalArgumentException("源人物不存在: " + request.getFromName()));

        PersonNode to = repository.findByName(request.getToName())
                .orElseThrow(() -> new IllegalArgumentException("目标人物不存在: " + request.getToName()));

        // 检查是否已存在关系，避免重复
        boolean alreadyKnows = from.getKnows().stream()
                .anyMatch(rel -> rel.getTarget().getName().equals(request.getToName()));
        if (alreadyKnows) {
            throw new IllegalStateException(request.getFromName() + " 已经认识 " + request.getToName());
        }

        from.getKnows().add(new KnowsRelationship(request.getSince(), to));
        repository.save(from);
    }

    /**
     * 查询直接朋友（1跳遍历）
     * <p>
     * 通过 SDN 加载聚合根，在 Java 中导航 knows 列表。
     */
    public List<PersonNode> findFriends(String name) {
        PersonNode person = findByName(name);
        return person.getKnows().stream()
                .map(KnowsRelationship::getTarget)
                .toList();
    }

    /**
     * 查询朋友的朋友（2跳遍历）
     * <p>
     * 使用自定义 Cypher 查询，展示图数据库多跳遍历能力。
     */
    public List<PersonNode> findFriendsOfFriends(String name) {
        // 先验证人物存在
        findByName(name);
        return repository.findFriendsOfFriends(name);
    }

    /**
     * 删除人物节点
     */
    public void deleteByName(String name) {
        PersonNode person = findByName(name);
        repository.delete(person);
    }
}
