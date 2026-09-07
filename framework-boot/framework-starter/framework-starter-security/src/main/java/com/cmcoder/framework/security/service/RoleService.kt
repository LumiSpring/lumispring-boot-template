package com.lumispring.framework.security.service

import com.baomidou.mybatisplus.extension.service.IService
import com.lumispring.framework.security.model.dto.RoleDTO
import com.lumispring.framework.security.model.dto.RoleUpdateDTO
import com.lumispring.framework.security.model.entity.SysRole
import com.lumispring.framework.security.model.vo.RoleVO

/**
 * 角色服务接口
 */
interface RoleService : IService<SysRole> {

    /**
     * 创建角色
     *
     * @param roleDTO 角色信息
     * @return 创建后的角色
     */
    fun createRole(roleDTO: RoleDTO): RoleVO

    /**
     * 更新角色
     *
     * @param id 角色ID
     * @param roleDTO 角色信息
     * @return 更新后的角色
     */
    fun updateRole(id: Long, roleDTO: RoleUpdateDTO): RoleVO

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    fun deleteRole(id: Long)

    /**
     * 根据ID查询角色
     *
     * @param id 角色ID
     * @return 角色信息
     */
    fun getRoleById(id: Long): RoleVO?

    /**
     * 根据角色编码查询角色
     *
     * @param roleCode 角色编码
     * @return 角色信息
     */
    fun getRoleByCode(roleCode: String): SysRole?

    /**
     * 查询所有启用的角色
     *
     * @return 角色列表
     */
    fun listAllEnabledRoles(): List<RoleVO>

    /**
     * 根据用户ID查询角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    fun getRolesByUserId(userId: Long): List<RoleVO>

    /**
     * 根据用户ID查询角色编码列表
     *
     * @param userId 用户ID
     * @return 角色编码列表
     */
    fun getRoleCodesByUserId(userId: Long): List<String>

    /**
     * 为用户分配角色
     *
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    fun assignRolesToUser(userId: Long, roleIds: List<Long>)

    /**
     * 检查角色编码是否存在
     *
     * @param roleCode 角色编码
     * @return true-存在，false-不存在
     */
    fun checkRoleCodeExists(roleCode: String): Boolean
}
