-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `password` VARCHAR(128) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
    `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `status` TINYINT NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
    `user_type` TINYINT NOT NULL DEFAULT '1' COMMENT '用户类型：1-普通用户，2-管理员（仅展示，鉴权以 admin 角色为准）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name` VARCHAR(64) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(64) NOT NULL COMMENT '角色编码',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `status` TINYINT NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 权限表
CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `name` VARCHAR(64) NOT NULL COMMENT '权限名称',
    `code` VARCHAR(64) NOT NULL COMMENT '权限编码：资源:动作，如 user:create',
    `path` VARCHAR(255) DEFAULT NULL COMMENT '权限路径：路由/API',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父级权限ID',
    `type` VARCHAR(32) NOT NULL COMMENT '权限类型：api-接口、menu-菜单、button-按钮',
    `component` VARCHAR(255) DEFAULT NULL COMMENT '组件路径',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `status` TINYINT NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status` (`status`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限表';

-- 用户直赋权限表
CREATE TABLE IF NOT EXISTS `sys_user_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_permission` (`user_id`, `permission_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户权限表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS `sys_operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人名称',
    `module` VARCHAR(64) NOT NULL COMMENT '模块：user/role/permission',
    `action` VARCHAR(32) NOT NULL COMMENT '操作类型：create/update/delete/assign',
    `target_id` BIGINT DEFAULT NULL COMMENT '目标数据ID',
    `target_name` VARCHAR(128) DEFAULT NULL COMMENT '目标数据名称',
    `detail` JSON DEFAULT NULL COMMENT '操作数据快照',
    `ip` VARCHAR(64) DEFAULT NULL COMMENT '操作IP',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_operator_id` (`operator_id`),
    KEY `idx_module` (`module`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 内置角色
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`, `status`) VALUES
(1, '管理员', 'admin', '系统管理员，绕过权限编码校验', 1),
(2, '普通用户', 'user', '普通用户', 1)
ON DUPLICATE KEY UPDATE `role_name` = VALUES(`role_name`);

-- 开发种子管理员（密码：admin123，BCrypt）。对外部署前请替换或删除。
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `email`, `status`, `user_type`) VALUES
(1, 'admin', '$2a$10$ZN6SJegRNpgzwGWAIWRRQuwJBdFDDybBeaERBO7TawAb7zAWEPbN2', '管理员', 'admin@example.com', 1, 2)
ON DUPLICATE KEY UPDATE `username` = VALUES(`username`);

INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1)
ON DUPLICATE KEY UPDATE `user_id` = VALUES(`user_id`);

-- 内置权限（管理端接口）
INSERT INTO `sys_permission` (`id`, `name`, `code`, `path`, `parent_id`, `type`, `description`, `status`) VALUES
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
(23, '用户赋权', 'permission:assign-user', '/admin/api/permission/assign/user/{userId}', 15, 'api', '为用户直赋权限', 1)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `path` = VALUES(`path`), `parent_id` = VALUES(`parent_id`);

-- 管理员角色拥有全部内置权限（admin 角色本身也会绕过编码校验）
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, `id` FROM `sys_permission` WHERE `id` BETWEEN 1 AND 23
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`);
