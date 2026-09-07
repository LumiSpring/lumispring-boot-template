package com.lumispring.framework.security.service

import com.baomidou.mybatisplus.extension.service.IService
import com.lumispring.framework.security.model.entity.SysUserRole

/**
 * 用户角色服务
 */
interface UserRoleService : IService<SysUserRole> {

    /**
     * 添加角色到用户
     * @param roleId 角色ID
     * @param userId 用户ID
     */
    fun addRoleToUser(roleId: Long, userId: Long): Boolean

    /**
     * 从用户移除角色
     * @param roleId 角色ID
     * @param userId 用户ID
     */
    fun removeRoleFromUser(roleId: Long, userId: Long): Boolean

    /**
     * 重设用户角色
     * @param roleIds 角色ID列表
     * @param userId 用户ID
     */
    fun resetRolesToUser(roleIds: List<Long>, userId: Long): Boolean

}