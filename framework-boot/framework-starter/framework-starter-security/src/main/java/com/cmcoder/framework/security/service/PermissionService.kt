package com.lumispring.framework.security.service

import com.baomidou.mybatisplus.extension.service.IService
import com.lumispring.framework.security.model.dto.PermissionDTO
import com.lumispring.framework.security.model.dto.PermissionQueryDTO
import com.lumispring.framework.security.model.dto.PermissionUpdateDTO
import com.lumispring.framework.security.model.entity.SysPermission
import com.lumispring.framework.security.model.vo.PermissionVO

/**
 * 权限服务接口
 */
interface PermissionService : IService<SysPermission> {

    /**
     * 创建权限
     *
     * @param permissionDTO 权限信息
     * @return 创建后的权限
     */
    fun createPermission(permissionDTO: PermissionDTO): PermissionVO

    /**
     * 更新权限
     *
     * @param id 权限ID
     * @param permissionDTO 权限信息
     * @return 更新后的权限
     */
    fun updatePermission(id: Long, permissionDTO: PermissionUpdateDTO): PermissionVO

    /**
     * 删除权限
     *
     * @param id 权限ID
     */
    fun deletePermission(id: Long)

    /**
     * 根据ID查询权限
     *
     * @param id 权限ID
     * @return 权限信息
     */
    fun getPermissionById(id: Long): PermissionVO?

    /**
     * 根据权限编码查询权限
     *
     * @param code 权限编码
     * @return 权限信息
     */
    fun getPermissionByCode(code: String): SysPermission?

    /**
     * 条件查询权限列表
     *
     * @param queryDTO 查询条件
     * @return 权限列表
     */
    fun listPermissions(queryDTO: PermissionQueryDTO): List<PermissionVO>

    /**
     * 查询权限树形结构
     *
     * @return 权限树
     */
    fun getPermissionTree(): List<PermissionVO>

    /**
     * 查询所有启用的权限
     *
     * @return 权限列表
     */
    fun listAllEnabledPermissions(): List<PermissionVO>

    /**
     * 根据角色ID查询权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    fun getPermissionsByRoleId(roleId: Long): List<PermissionVO>

    /**
     * 根据用户ID查询权限列表（包含角色权限和直接权限）
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    fun getPermissionsByUserId(userId: Long): List<PermissionVO>

    /**
     * 根据用户ID查询权限编码列表
     *
     * @param userId 用户ID
     * @return 权限编码列表
     */
    fun getPermissionCodesByUserId(userId: Long): List<String>

    /**
     * 为角色分配权限
     *
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     */
    fun assignPermissionsToRole(roleId: Long, permissionIds: List<Long>)

    /**
     * 为用户直接分配权限
     *
     * @param userId 用户ID
     * @param permissionIds 权限ID列表
     */
    fun assignPermissionsToUser(userId: Long, permissionIds: List<Long>)

    /**
     * 检查权限编码是否存在
     *
     * @param code 权限编码
     * @return true-存在，false-不存在
     */
    fun checkPermissionCodeExists(code: String): Boolean

    /**
     * 检查用户是否拥有指定权限
     *
     * @param userId 用户ID
     * @param code 权限编码
     * @return true-拥有，false-没有
     */
    fun hasPermission(userId: Long, code: String): Boolean
}
