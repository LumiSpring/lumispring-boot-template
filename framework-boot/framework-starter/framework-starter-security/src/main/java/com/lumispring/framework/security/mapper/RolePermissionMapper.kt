package com.lumispring.framework.security.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.lumispring.framework.security.model.entity.SysRolePermission
import org.apache.ibatis.annotations.Delete
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import org.springframework.transaction.annotation.Transactional

/**
 * 角色权限关联数据访问层
 */
@Mapper
@Transactional(transactionManager = "securityTransactionManager")
interface RolePermissionMapper : BaseMapper<SysRolePermission> {

    /**
     * 根据角色ID删除所有权限关联
     *
     * @param roleId 角色ID
     * @return 删除数量
     */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    fun deleteByRoleId(@Param("roleId") roleId: Long): Long

    /**
     * 根据权限ID删除所有角色关联
     *
     * @param permissionId 权限ID
     * @return 删除数量
     */
    @Delete("DELETE FROM sys_role_permission WHERE permission_id = #{permissionId}")
    fun deleteByPermissionId(@Param("permissionId") permissionId: Long): Long

    /**
     * 根据角色ID查询权限ID列表
     *
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    @Select("SELECT permission_id FROM sys_role_permission WHERE role_id = #{roleId}")
    fun selectPermissionIdsByRoleId(@Param("roleId") roleId: Long): List<Long>
}
