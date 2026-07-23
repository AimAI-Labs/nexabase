package io.github.aimailabs.nexabase.graph.controller;

import io.github.aimailabs.nexabase.foundation.common.Result;
import io.github.aimailabs.nexabase.graph.dto.CreateRelationshipRequest;
import io.github.aimailabs.nexabase.graph.dto.PersonCreateRequest;
import io.github.aimailabs.nexabase.graph.dto.PersonResponse;
import io.github.aimailabs.nexabase.graph.entity.PersonNode;
import io.github.aimailabs.nexabase.graph.service.PersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识图谱 / Person 图谱接口
 * <p>
 * 提供人物节点的 CRUD 和关系遍历接口，
 * 演示 Neo4j 图数据库的核心概念。
 *
 * @module nexabase-graph
 */
@Tag(name = "Person 图谱", description = "人物节点 CRUD 与 KNOWS 关系遍历")
@RestController
@RequestMapping("/graph/persons")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    /**
     * 创建人物节点。
     *
     * @param request 创建人物请求体（姓名、年龄）
     * @return 创建成功的人物响应
     */
    @Operation(summary = "创建人物节点")
    @PostMapping
    public Result<PersonResponse> createPerson(@RequestBody PersonCreateRequest request) {
        PersonNode person = personService.createPerson(request);
        return Result.success(toResponse(person));
    }

    /**
     * 查询所有人物节点。
     *
     * @return 人物节点列表
     */
    @Operation(summary = "查询所有人物")
    @GetMapping
    public Result<List<PersonResponse>> findAll() {
        List<PersonResponse> list = personService.findAll().stream()
                .map(this::toResponse)
                .toList();
        return Result.success(list);
    }

    /**
     * 按姓名查询人物节点。
     *
     * @param name 人物姓名
     * @return 人物节点详情
     */
    @Operation(summary = "按姓名查询人物")
    @GetMapping("/{name}")
    public Result<PersonResponse> findByName(@PathVariable("name") String name) {
        PersonNode person = personService.findByName(name);
        return Result.success(toResponse(person));
    }

    /**
     * 建立 KNOWS 关系，创建有向关系：from 认识 to，附带 since 属性。
     *
     * @param request 关系创建请求体（源/目标人物姓名、关系属性 since）
     * @return 成功响应
     */
    @Operation(summary = "建立 KNOWS 关系", description = "创建有向关系：from 认识 to，附带 since 属性")
    @PostMapping("/relationship")
    public Result<Void> createRelationship(@RequestBody CreateRelationshipRequest request) {
        personService.createRelationship(request);
        return Result.success();
    }

    /**
     * 查询直接朋友（1 跳遍历），返回该人物通过 KNOWS 关系直接认识的人。
     *
     * @param name 人物姓名
     * @return 直接朋友列表
     */
    @Operation(summary = "查询直接朋友（1跳遍历）", description = "返回该人物通过 KNOWS 关系直接认识的人")
    @GetMapping("/{name}/friends")
    public Result<List<PersonResponse>> findFriends(@PathVariable("name") String name) {
        List<PersonResponse> friends = personService.findFriends(name).stream()
                .map(this::toResponse)
                .toList();
        return Result.success(friends);
    }

    /**
     * 查询朋友的朋友（2 跳遍历），图数据库经典多跳关系场景。
     *
     * @param name 人物姓名
     * @return 朋友的朋友列表
     */
    @Operation(summary = "查询朋友的朋友（2跳遍历）", description = "图数据库经典场景：多跳关系遍历")
    @GetMapping("/{name}/friends-of-friends")
    public Result<List<PersonResponse>> findFriendsOfFriends(@PathVariable("name") String name) {
        List<PersonResponse> fofs = personService.findFriendsOfFriends(name).stream()
                .map(this::toResponse)
                .toList();
        return Result.success(fofs);
    }

    /**
     * 删除人物节点（含其关联关系）。
     *
     * @param name 人物姓名
     * @return 成功响应
     */
    @Operation(summary = "删除人物节点")
    @DeleteMapping("/{name}")
    public Result<Void> deleteByName(@PathVariable("name") String name) {
        personService.deleteByName(name);
        return Result.success();
    }

    /**
     * 实体转 DTO，避免序列化循环引用。
     *
     * @param person 人物节点实体
     * @return 人物响应 DTO
     */
    private PersonResponse toResponse(PersonNode person) {
        return new PersonResponse(person.getId(), person.getName(), person.getAge());
    }
}
