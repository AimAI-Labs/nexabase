package io.github.aimailabs.nexabase.graph.controller;

import io.github.aimailabs.nexabase.graph.common.Result;
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
 * Person 图谱接口
 * <p>
 * 提供人物节点的 CRUD 和关系遍历接口，
 * 演示 Neo4j 图数据库的核心概念。
 */
@Tag(name = "Person 图谱", description = "人物节点 CRUD 与 KNOWS 关系遍历")
@RestController
@RequestMapping("/graph/persons")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @Operation(summary = "创建人物节点")
    @PostMapping
    public Result<PersonResponse> createPerson(@RequestBody PersonCreateRequest request) {
        PersonNode person = personService.createPerson(request);
        return Result.success(toResponse(person));
    }

    @Operation(summary = "查询所有人物")
    @GetMapping
    public Result<List<PersonResponse>> findAll() {
        List<PersonResponse> list = personService.findAll().stream()
                .map(this::toResponse)
                .toList();
        return Result.success(list);
    }

    @Operation(summary = "按姓名查询人物")
    @GetMapping("/{name}")
    public Result<PersonResponse> findByName(@PathVariable("name") String name) {
        PersonNode person = personService.findByName(name);
        return Result.success(toResponse(person));
    }

    @Operation(summary = "建立 KNOWS 关系", description = "创建有向关系：from 认识 to，附带 since 属性")
    @PostMapping("/relationship")
    public Result<Void> createRelationship(@RequestBody CreateRelationshipRequest request) {
        personService.createRelationship(request);
        return Result.success();
    }

    @Operation(summary = "查询直接朋友（1跳遍历）", description = "返回该人物通过 KNOWS 关系直接认识的人")
    @GetMapping("/{name}/friends")
    public Result<List<PersonResponse>> findFriends(@PathVariable("name") String name) {
        List<PersonResponse> friends = personService.findFriends(name).stream()
                .map(this::toResponse)
                .toList();
        return Result.success(friends);
    }

    @Operation(summary = "查询朋友的朋友（2跳遍历）", description = "图数据库经典场景：多跳关系遍历")
    @GetMapping("/{name}/friends-of-friends")
    public Result<List<PersonResponse>> findFriendsOfFriends(@PathVariable("name") String name) {
        List<PersonResponse> fofs = personService.findFriendsOfFriends(name).stream()
                .map(this::toResponse)
                .toList();
        return Result.success(fofs);
    }

    @Operation(summary = "删除人物节点")
    @DeleteMapping("/{name}")
    public Result<Void> deleteByName(@PathVariable("name") String name) {
        personService.deleteByName(name);
        return Result.success();
    }

    /**
     * 实体转 DTO，避免序列化循环引用
     */
    private PersonResponse toResponse(PersonNode person) {
        return new PersonResponse(person.getId(), person.getName(), person.getAge());
    }
}
