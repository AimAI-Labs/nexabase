-- ============================================================
-- Nexabase Auth 模块测试模拟数据
-- ============================================================
-- 版本: V3
-- 命名规范: V{版本号}__{动词}_{对象}.sql （Flyway 默认约定）
--   - 版本号严格递增，不跳号、不复用
--   - 版本号用整数（非时间戳），保持与 V1/V2 一致
--   - 双下划线分隔版本号与描述
-- 说明: 为开发/测试环境注入覆盖 RBAC 全链路的模拟数据，
--       包含用户、角色、权限、团队及关联关系、操作日志。
-- 幂等性: 所有 INSERT 使用 INSERT IGNORE，遇主键/唯一键冲突时静默跳过，
--         保证脚本可重复执行不报错。
-- 密码:  password_hash 使用占位符 INIT_PASSWORD_HASH，
--         由 PasswordDataInitializer 启动时统一替换为 BCrypt(123456)。
-- ============================================================

-- -------------------------------------------------------
-- 1. 角色（id: 2-5）
--    覆盖场景：正常角色 / 无权限角色 / 停用角色 / 异常权限配置角色
-- -------------------------------------------------------
INSERT IGNORE INTO `sys_role` (`id`, `name`, `code`, `description`, `status`, `created_by`, `updated_by`) VALUES
    (2, '内容管理员', 'CONTENT_ADMIN', '拥有系统模块只读权限，用于测试正常角色权限链路', 1, 1, 1),
    (3, '普通用户',  'USER',          '启用但无任何权限映射（边界：角色存在但权限为空）', 1, 1, 1),
    (4, '访客',      'GUEST',         '已停用角色，但仍挂载权限映射（边界：停用角色+异常权限配置）', 0, 1, 1),
    (5, '废弃角色',  'DEPRECATED_ROLE','已停用且挂载写权限（边界：停用角色持有高危权限的异常场景）', 0, 1, 1);

-- -------------------------------------------------------
-- 2. 团队/组织（id: 10-15）
--    覆盖场景：多层级部门 / 项目组 / 逻辑删除的团队
-- -------------------------------------------------------
INSERT IGNORE INTO `sys_team` (`id`, `parent_id`, `name`, `type`, `sort_order`, `created_by`, `updated_by`, `is_deleted`) VALUES
    (10, 0,  '集团总部',   1, 1,  1, 1, 0),
    (11, 10, '研发中心',   1, 1,  1, 1, 0),
    (12, 10, '产品中心',   1, 2,  1, 1, 0),
    (13, 11, '测试组',     1, 1,  1, 1, 0),
    (14, 11, 'KAG项目组',  2, 1,  1, 1, 0),
    (15, 0,  '已解散部门',  1, 99, 1, 1, 1);  -- 边界：逻辑删除的团队

-- -------------------------------------------------------
-- 3. 用户（id: 2-10）
--    覆盖场景：正常 / 禁用 / 无角色 / 多角色 / 逻辑删除 / 特殊字符 / 无团队
-- -------------------------------------------------------
INSERT IGNORE INTO `sys_user` (`id`, `username`, `password_hash`, `nickname`, `email`, `phone`, `status`, `jwt_version`, `created_by`, `updated_by`, `is_deleted`) VALUES
    (2,  'content_mgr',   'INIT_PASSWORD_HASH', '内容管理员',  'content@nexabase.com',  '13800000002', 1, 1, 1, 1, 0),
    (3,  'user_normal',   'INIT_PASSWORD_HASH', '普通用户',    'normal@nexabase.com',   '13800000003', 1, 1, 1, 1, 0),
    (4,  'user_disabled', 'INIT_PASSWORD_HASH', '已禁用用户',  'disabled@nexabase.com', '13800000004', 0, 1, 1, 1, 0),  -- 边界：禁用状态
    (5,  'user_guest',    'INIT_PASSWORD_HASH', '访客用户',    'guest@nexabase.com',    '13800000005', 0, 1, 1, 1, 0),  -- 边界：禁用+关联停用角色
    (6,  'user_norole',   'INIT_PASSWORD_HASH', '无角色用户',  'norole@nexabase.com',   '13800000006', 1, 1, 1, 1, 0),  -- 边界：正常但无任何角色
    (7,  'user_multi',    'INIT_PASSWORD_HASH', '多角色用户',  'multi@nexabase.com',    '13800000007', 1, 1, 1, 1, 0),  -- 边界：同时持有多个角色+多团队
    (8,  'user_deleted',   'INIT_PASSWORD_HASH', '已删除用户',  'deleted@nexabase.com',  '13800000008', 1, 1, 1, 1, 1),  -- 边界：逻辑删除
    (9,  'user_special',  'INIT_PASSWORD_HASH', '特殊''字符''用户', 'special+test@nexabase.com', '13800000009', 1, 1, 1, 1, 0),  -- 边界：特殊字符
    (10, 'user_noteam',   'INIT_PASSWORD_HASH', '无团队用户',  'noteam@nexabase.com',   NULL,          1, 1, 1, 1, 0);  -- 边界：phone 为 NULL + 无团队

-- -------------------------------------------------------
-- 4. 用户-角色关联
--    覆盖场景：单角色 / 多角色 / 禁用用户关联角色 / 逻辑删除用户关联角色 / 无角色
-- -------------------------------------------------------
INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`) VALUES
    (2, 2),   -- content_mgr → CONTENT_ADMIN
    (3, 3),   -- user_normal → USER
    (4, 3),   -- user_disabled → USER（禁用用户仍有关联）
    (5, 4),   -- user_guest → GUEST（禁用用户→停用角色）
    (7, 2),   -- user_multi → CONTENT_ADMIN
    (7, 3),   -- user_multi → USER（多角色）
    (8, 3),   -- user_deleted → USER（逻辑删除用户仍有关联）
    (9, 3),   -- user_special → USER
    (10, 3);  -- user_noteam → USER
-- 用户 6 (user_norole) 故意不插入任何角色关联（边界）

-- -------------------------------------------------------
-- 5. 用户-团队关联
--    覆盖场景：团队负责人 / 普通成员 / 跨团队 / 无团队
-- -------------------------------------------------------
INSERT IGNORE INTO `sys_user_team` (`user_id`, `team_id`, `is_leader`) VALUES
    (2, 11, 1),   -- content_mgr → 研发中心（负责人）
    (3, 13, 0),   -- user_normal → 测试组（普通成员）
    (4, 12, 0),   -- user_disabled → 产品中心
    (5, 12, 0),   -- user_guest → 产品中心
    (7, 11, 0),   -- user_multi → 研发中心
    (7, 14, 1),   -- user_multi → KAG项目组（负责人，跨团队）
    (8, 13, 0),   -- user_deleted → 测试组
    (9, 14, 0);   -- user_special → KAG项目组
-- 用户 6 (user_norole) 和用户 10 (user_noteam) 故意不插入团队关联（边界）

-- -------------------------------------------------------
-- 6. 团队-角色关联（权限继承核心）
--    覆盖场景：部门继承角色 / 项目组继承角色
-- -------------------------------------------------------
INSERT IGNORE INTO `sys_team_role` (`team_id`, `role_id`) VALUES
    (11, 2),   -- 研发中心 → CONTENT_ADMIN
    (13, 3),   -- 测试组 → USER
    (14, 3);   -- KAG项目组 → USER

-- -------------------------------------------------------
-- 7. 角色-权限关联
--    覆盖场景：管理员全权限 / 只读权限 / 无权限 / 停用角色持有权限（异常配置）
--    权限 ID 引用 V2 中已插入的菜单(100-104)与按钮权限(1101-1405)
-- -------------------------------------------------------
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
    -- SUPER_ADMIN(1): 全权限（菜单 + 按钮）
    (1, 100),  -- 系统管理
    (1, 101),  -- 用户管理
    (1, 102),  -- 角色管理
    (1, 103),  -- 权限管理
    (1, 104),  -- 团队管理
    (1, 1101), -- sys:user:add
    (1, 1102), -- sys:user:edit
    (1, 1103), -- sys:user:delete
    (1, 1104), -- sys:user:list
    (1, 1105), -- sys:user:resetPassword
    (1, 1106), -- sys:user:assignRoles
    (1, 1201), -- sys:role:add
    (1, 1202), -- sys:role:edit
    (1, 1203), -- sys:role:delete
    (1, 1204), -- sys:role:list
    (1, 1205), -- sys:role:assignPermissions
    (1, 1301), -- sys:permission:add
    (1, 1302), -- sys:permission:edit
    (1, 1303), -- sys:permission:delete
    (1, 1304), -- sys:permission:list
    (1, 1401), -- sys:team:add
    (1, 1402), -- sys:team:edit
    (1, 1403), -- sys:team:delete
    (1, 1404), -- sys:team:list
    (1, 1405), -- sys:team:assignRoles
    -- CONTENT_ADMIN(2): 只读权限集
    (2, 1104),  -- sys:user:list
    (2, 1204),  -- sys:role:list
    (2, 1304),  -- sys:permission:list
    (2, 1404),  -- sys:team:list
    -- GUEST(4): 停用角色但仍挂载查询权限（异常配置边界）
    (4, 1104),  -- sys:user:list
    -- DEPRECATED_ROLE(5): 停用角色挂载写权限（高危异常配置边界）
    (5, 1101),  -- sys:user:add
    (5, 1104);  -- sys:user:list
-- USER(3): 故意不插入任何权限映射（边界：启用角色但无权限）

-- -------------------------------------------------------
-- 8. 操作日志（id: 1-5）
--    覆盖场景：成功 / 失败 / 禁用用户操作 / 无权限访问 / 匿名用户
-- -------------------------------------------------------
INSERT IGNORE INTO `sys_oper_log` (`id`, `user_id`, `username`, `module`, `action`, `method`, `url`, `ip_address`, `request_params`, `response_result`, `status`, `error_msg`, `cost_time`) VALUES
    (1, 1,    'admin',         '用户管理', '用户新增', 'POST', '/api/v1/sys/users',  '127.0.0.1',     '{"username":"user_normal"}',  '{"code":200,"message":"success"}', 1, NULL,                 120),
    (2, 2,    'content_mgr',   '角色管理', '角色查询', 'GET',  '/api/v1/sys/roles',  '192.168.1.100', '{}',                          '{"code":200,"data":[]}',          1, NULL,                 35),
    (3, 4,    'user_disabled', '认证',     '登录',     'POST', '/api/v1/auth/login', '10.0.0.50',     '{"username":"user_disabled"}','{"code":403,"message":"账号已禁用"}', 0, '账号已禁用',          45),
    (4, 6,    'user_norole',   '权限校验', '接口访问', 'GET',  '/api/v1/sys/users',  '10.0.0.60',     '{}',                          '{"code":403,"message":"权限不足"}', 0, '权限不足',            20),
    (5, NULL, 'anonymous',      '认证',     '登录',     'POST', '/api/v1/auth/login', '10.0.0.99',     '{"username":"unknown"}',      '{"code":401,"message":"用户名或密码错误"}', 0, '用户名或密码错误',  30);
