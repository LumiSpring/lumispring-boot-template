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
    `user_type` TINYINT NOT NULL DEFAULT '1' COMMENT '用户类型：1-普通用户，2-管理员',
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

-- 插入默认角色
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`, `status`) VALUES
(1, '管理员', 'admin', '系统管理员', 1),
(2, '普通用户', 'user', '普通用户', 1)
ON DUPLICATE KEY UPDATE `role_name` = VALUES(`role_name`);

-- 插入默认管理员用户（密码：admin123，需使用BCrypt加密）
-- 注意：实际使用时请替换为加密后的密码
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `email`, `status`, `user_type`) VALUES
(1, 'admin', '$2a$10$ZN6SJegRNpgzwGWAIWRRQuwJBdFDDybBeaERBO7TawAb7zAWEPbN2', '管理员', 'admin@example.com', 1, 2)
ON DUPLICATE KEY UPDATE `username` = VALUES(`username`);

-- 关联管理员角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1) ON DUPLICATE KEY UPDATE `user_id` = VALUES(`user_id`);


-- 设计权限表
CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT primary key COMMENT '权限ID',
    `name` VARCHAR(64) NOT NULL COMMENT '权限名称',
    `code` VARCHAR(64) NOT NULL unique COMMENT '权限编码：【模块:资源:名称】',
    `path` VARCHAR(255) DEFAULT NULL COMMENT '权限路径：路由/API',
    parent_id BIGINT DEFAULT NULL COMMENT '父级权限ID',
    `type` varchar(32) NOT NULL COMMENT '权限类型：api-接口、menu-菜单、button-按钮',
    component varchar(255) DEFAULT NULL COMMENT '组件路径',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `status` TINYINT NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL comment '创建人',
    update_by BIGINT DEFAULT NULL comment '更新人'
) comment '权限表';


create table if not exists `sys_role_permission` (
    `id` bigint not null auto_increment comment 'id',
    `role_id` bigint not null comment '角色id',
    `permission_id` bigint not null comment '权限id',
    primary key (`id`),
    unique key `uk_role_permission` (`role_id`, `permission_id`)
) comment '角色权限表';

create table if not exists `sys_user_permission` (
    `id` bigint not null auto_increment comment 'id',
    `user_id` bigint not null comment '用户id',
    `permission_id` bigint not null comment '权限id',
    primary key (`id`),
    unique key `uk_user_permission` (`user_id`, `permission_id`)
) comment '用户权限表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS `sys_operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人名称',
    `module` VARCHAR(64) NOT NULL COMMENT '模块：user/role/permission',
    `action` VARCHAR(32) NOT NULL COMMENT '操作类型：create/update/delete',
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