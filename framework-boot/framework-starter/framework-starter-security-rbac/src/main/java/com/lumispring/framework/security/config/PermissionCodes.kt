package com.lumispring.framework.security.config

/**
 * 内置管理接口的权限编码。后端鉴权只认 code，与菜单 path 无关。
 */
object PermissionCodes {
    const val USER_CREATE = "user:create"
    const val USER_GET = "user:get"
    const val USER_LIST = "user:list"
    const val USER_UPDATE = "user:update"
    const val USER_DELETE = "user:delete"
    const val USER_STATUS = "user:status"

    const val ROLE_CREATE = "role:create"
    const val ROLE_GET = "role:get"
    const val ROLE_LIST = "role:list"
    const val ROLE_UPDATE = "role:update"
    const val ROLE_DELETE = "role:delete"
    const val ROLE_ASSIGN = "role:assign"

    const val PERMISSION_CREATE = "permission:create"
    const val PERMISSION_GET = "permission:get"
    const val PERMISSION_LIST = "permission:list"
    const val PERMISSION_UPDATE = "permission:update"
    const val PERMISSION_DELETE = "permission:delete"
    const val PERMISSION_TREE = "permission:tree"
    const val PERMISSION_ASSIGN_ROLE = "permission:assign-role"
    const val PERMISSION_ASSIGN_USER = "permission:assign-user"
}
