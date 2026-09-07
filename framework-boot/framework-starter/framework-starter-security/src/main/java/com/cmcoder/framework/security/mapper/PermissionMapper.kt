package com.lumispring.framework.security.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.lumispring.framework.security.model.entity.SysPermission
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import org.springframework.transaction.annotation.Transactional

/**
 * 权限数据访问层
 */
@Mapper
@Transactional(transactionManager = "securityTransactionManager")
interface PermissionMapper : BaseMapper<SysPermission> {

    /**
     * 根据角色ID查询权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Select("""
        SELECT p.* FROM sys_permission p
        INNER JOIN sys_role_permission rp ON p.id = rp.permission_id
        WHERE rp.role_id = #{roleId} AND p.status = 1
        ORDER BY p.create_time ASC
    """)
    fun selectPermissionsByRoleId(@Param("roleId") roleId: Long): List<SysPermission>

    /**
     * 根据用户ID查询权限列表（通过角色关联）
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Select("""
        SELECT DISTINCT p.* FROM sys_permission p
        INNER JOIN sys_role_permission rp ON p.id = rp.permission_id
        INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id
        WHERE ur.user_id = #{userId} AND p.status = 1
        ORDER BY p.create_time ASC
    """)
    fun selectPermissionsByUserId(@Param("userId") userId: Long): List<SysPermission>

    /**
     * 根据用户ID查询权限编码列表（通过角色关联）
     *
     * @param userId 用户ID
     * @return 权限编码列表
     */
    @Select("""
        SELECT DISTINCT p.code FROM sys_permission p
        INNER JOIN sys_role_permission rp ON p.id = rp.permission_id
        INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id
        WHERE ur.user_id = #{userId} AND p.status = 1
    """)
    fun selectPermissionCodesByUserId(@Param("userId") userId: Long): List<String>

    /**
     * 根据用户ID查询直接分配的权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Select("""
        SELECT p.* FROM sys_permission p
        INNER JOIN sys_user_permission up ON p.id = up.permission_id
        WHERE up.user_id = #{userId} AND p.status = 1
        ORDER BY p.create_time ASC
    """)
    fun selectDirectPermissionsByUserId(@Param("userId") userId: Long): List<SysPermission>

    /**
     * 根据权限编码查询权限
     *
     * @param code 权限编码
     * @return 权限
     */
    @Select("SELECT * FROM sys_permission WHERE code = #{code}")
    fun selectByCode(@Param("code") code: String): SysPermission?
}
