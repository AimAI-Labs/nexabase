-- ============================================================
-- Nexabase RBAC 初始化种子数据
-- 包含：超管角色、超管用户、权限树种子、关联绑定
-- ============================================================

-- -------------------------------------------------------
-- 超管角色
-- -------------------------------------------------------
INSERT INTO `sys_role` (`id`, `name`, `code`, `description`, `status`)
VALUES (1, '超级管理员', 'SUPER_ADMIN', '系统内置超级管理员，拥有全部权限', 1);

-- -------------------------------------------------------
-- 超管用户
-- 密码占位符由 PasswordDataInitializer 启动时替换为 BCrypt(123456)
-- -------------------------------------------------------
INSERT INTO `sys_user` (`id`, `username`, `password_hash`, `nickname`, `status`, `jwt_version`)
VALUES (1, 'admin', 'INIT_PASSWORD_HASH', '超级管理员', 1, 1);

-- 绑定 admin ↔ SUPER_ADMIN
INSERT INTO `sys_user_role` (`user_id`, `role_id`)
VALUES (1, 1);

-- -------------------------------------------------------
-- 权限树种子：系统管理 → 用户/角色/权限/团队管理 → 各按钮权限
-- -------------------------------------------------------

-- 系统管理（一级菜单）
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `type`, `permission_key`, `path`, `component`, `icon`, `sort_order`)
VALUES (100, 0, '系统管理', 1, 'sys', '/sys', 'Layout', 'setting', 1);

-- 用户管理（二级菜单）
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `type`, `permission_key`, `path`, `component`, `icon`, `sort_order`)
VALUES (101, 100, '用户管理', 1, 'sys:user', '/sys/user', 'sys/user/index', 'user', 1);
-- 用户管理按钮权限
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `type`, `permission_key`, `sort_order`)
VALUES (1101, 101, '用户新增', 2, 'sys:user:add', 1),
       (1102, 101, '用户编辑', 2, 'sys:user:edit', 2),
       (1103, 101, '用户删除', 2, 'sys:user:delete', 3),
       (1104, 101, '用户查询', 2, 'sys:user:list', 4),
       (1105, 101, '重置密码', 2, 'sys:user:resetPassword', 5),
       (1106, 101, '分配角色', 2, 'sys:user:assignRoles', 6);

-- 角色管理（二级菜单）
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `type`, `permission_key`, `path`, `component`, `icon`, `sort_order`)
VALUES (102, 100, '角色管理', 1, 'sys:role', '/sys/role', 'sys/role/index', 'peoples', 2);
-- 角色管理按钮权限
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `type`, `permission_key`, `sort_order`)
VALUES (1201, 102, '角色新增', 2, 'sys:role:add', 1),
       (1202, 102, '角色编辑', 2, 'sys:role:edit', 2),
       (1203, 102, '角色删除', 2, 'sys:role:delete', 3),
       (1204, 102, '角色查询', 2, 'sys:role:list', 4),
       (1205, 102, '分配权限', 2, 'sys:role:assignPermissions', 5);

-- 权限管理（二级菜单）
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `type`, `permission_key`, `path`, `component`, `icon`, `sort_order`)
VALUES (103, 100, '权限管理', 1, 'sys:permission', '/sys/permission', 'sys/permission/index', 'tree-table', 3);
-- 权限管理按钮权限
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `type`, `permission_key`, `sort_order`)
VALUES (1301, 103, '权限新增', 2, 'sys:permission:add', 1),
       (1302, 103, '权限编辑', 2, 'sys:permission:edit', 2),
       (1303, 103, '权限删除', 2, 'sys:permission:delete', 3),
       (1304, 103, '权限查询', 2, 'sys:permission:list', 4);

-- 团队管理（二级菜单）
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `type`, `permission_key`, `path`, `component`, `icon`, `sort_order`)
VALUES (104, 100, '团队管理', 1, 'sys:team', '/sys/team', 'sys/team/index', 'organization', 4);
-- 团队管理按钮权限
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `type`, `permission_key`, `sort_order`)
VALUES (1401, 104, '团队新增', 2, 'sys:team:add', 1),
       (1402, 104, '团队编辑', 2, 'sys:team:edit', 2),
       (1403, 104, '团队删除', 2, 'sys:team:delete', 3),
       (1404, 104, '团队查询', 2, 'sys:team:list', 4),
       (1405, 104, '分配角色', 2, 'sys:team:assignRoles', 5);
