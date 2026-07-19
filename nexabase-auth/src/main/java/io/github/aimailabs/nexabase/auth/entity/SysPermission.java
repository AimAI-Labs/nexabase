package io.github.aimailabs.nexabase.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.github.aimailabs.nexabase.auth.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限资源实体（树形结构）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class SysPermission extends BaseEntity {

    /** 父权限 ID(0 为根) */
    private Long parentId;

    /** 权限名称 */
    private String name;

    /** 类型: 1-菜单, 2-按钮, 3-API 接口 */
    private Integer type;

    /** 权限标识(如 sys:user:add) */
    private String permissionKey;

    /** 路由路径或 API URL */
    private String path;

    /** 请求方法(API 权限用) */
    private String method;

    /** 前端组件路径(菜单权限用) */
    private String component;

    /** 菜单图标 */
    private String icon;

    /** 排序号 */
    private Integer sortOrder;
}
