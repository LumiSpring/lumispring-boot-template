-- 内置角色。INSERT IGNORE：已有同名编码时不覆盖。
INSERT IGNORE INTO `sys_role` (`id`, `role_name`, `role_code`, `description`, `status`) VALUES
(1, '管理员', 'admin', '系统管理员，绕过权限编码校验', 1),
(2, '普通用户', 'user', '普通用户', 1);

-- 开发种子管理员。对外部署前请替换或删除。
INSERT IGNORE INTO `sys_user` (`id`, `username`, `password`, `nickname`, `email`, `status`, `user_type`) VALUES
(1, 'admin', '$2a$10$ZN6SJegRNpgzwGWAIWRRQuwJBdFDDybBeaERBO7TawAb7zAWEPbN2', '管理员', 'admin@example.com', 1, 2);

INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- 内置权限（管理端接口）
INSERT IGNORE INTO `sys_permission` (`id`, `name`, `code`, `path`, `parent_id`, `type`, `description`, `status`) VALUES
(1, '用户管理', 'user', '/admin/api/user', NULL, 'menu', '用户管理', 1),
(2, '创建用户', 'user:create', '/admin/api/user', 1, 'api', '创建用户', 1),
(3, '查看用户', 'user:get', '/admin/api/user/{id}', 1, 'api', '查看用户详情', 1),
(4, '用户列表', 'user:list', '/admin/api/user/list', 1, 'api', '查询用户列表', 1),
(5, '更新用户', 'user:update', '/admin/api/user/{id}', 1, 'api', '更新用户信息', 1),
(6, '删除用户', 'user:delete', '/admin/api/user/{id}', 1, 'api', '删除用户', 1),
(7, '用户状态', 'user:status', '/admin/api/user/{id}/status', 1, 'api', '启用或禁用用户', 1),
(8, '角色管理', 'role', '/admin/api/role', NULL, 'menu', '角色管理', 1),
(9, '创建角色', 'role:create', '/admin/api/role', 8, 'api', '创建角色', 1),
(10, '查看角色', 'role:get', '/admin/api/role/{id}', 8, 'api', '查看角色详情', 1),
(11, '角色列表', 'role:list', '/admin/api/role/list', 8, 'api', '查询角色列表', 1),
(12, '更新角色', 'role:update', '/admin/api/role/{id}', 8, 'api', '更新角色', 1),
(13, '删除角色', 'role:delete', '/admin/api/role/{id}', 8, 'api', '删除角色', 1),
(14, '分配角色', 'role:assign', '/admin/api/role/assign/{userId}', 8, 'api', '为用户分配角色', 1),
(15, '权限管理', 'permission', '/admin/api/permission', NULL, 'menu', '权限管理', 1),
(16, '创建权限', 'permission:create', '/admin/api/permission', 15, 'api', '创建权限', 1),
(17, '查看权限', 'permission:get', '/admin/api/permission/{id}', 15, 'api', '查看权限详情', 1),
(18, '权限列表', 'permission:list', '/admin/api/permission/list', 15, 'api', '查询权限列表', 1),
(19, '更新权限', 'permission:update', '/admin/api/permission/{id}', 15, 'api', '更新权限', 1),
(20, '删除权限', 'permission:delete', '/admin/api/permission/{id}', 15, 'api', '删除权限', 1),
(21, '权限树', 'permission:tree', '/admin/api/permission/tree', 15, 'api', '查询权限树', 1),
(22, '角色赋权', 'permission:assign-role', '/admin/api/permission/assign/role/{roleId}', 15, 'api', '为角色分配权限', 1),
(23, '用户赋权', 'permission:assign-user', '/admin/api/permission/assign/user/{userId}', 15, 'api', '为用户直赋权限', 1);

-- 管理员角色拥有全部内置权限（admin 角色本身也会绕过编码校验）
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, `id` FROM `sys_permission` WHERE `id` BETWEEN 1 AND 23;
